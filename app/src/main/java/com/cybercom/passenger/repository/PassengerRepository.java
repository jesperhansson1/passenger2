package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.databasemodel.utils.DatabaseModelHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;

public class PassengerRepository implements PassengerRepositoryInterface {


    public static final String NOTIFICATION_TOKEN_ID = "notificationTokenId";

    private static final String REFERENCE_NOTIFICATIONS = "notifications";
    private static final String REFERENCE_USERS = "users";
    private static final String REFERENCE_DRIVES = "drives";
    private static final String REFERENCE_DRIVE_REQUESTS = "driveRequests";
    private static final String REFERENCE_USERS_CHILD_TYPE = "type";

    private static final String DRIVE_DRIVER_ID = "driveDriverId";
    private static final String DRIVE_REQUEST_PASSENGER = "driveRequestPassenger";

    private static final int DRIVE_REQUEST_MATCH_TIME_THRESHOLD = 15 * 60 * 60 * 1000;

    private static PassengerRepository sPassengerRepository;
    private DatabaseReference mUsersReference;
    private DatabaseReference mDrivesReference;
    private DatabaseReference mDriveRequestsReference;
    private DatabaseReference mNotificationsReference;

    BlockingQueue<Notification> mNotificationQueue = new LinkedBlockingQueue<>();

    private MutableLiveData<Notification> mNotification = new MutableLiveData<>();

    public static PassengerRepository getInstance() {
        if (sPassengerRepository == null) {
            sPassengerRepository = new PassengerRepository();
        }
        return sPassengerRepository;
    }

    private PassengerRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = firebaseDatabase.getReference(REFERENCE_USERS);
        mDrivesReference = firebaseDatabase.getReference(REFERENCE_DRIVES);
        mDriveRequestsReference = firebaseDatabase.getReference(REFERENCE_DRIVE_REQUESTS);
        mNotificationsReference = firebaseDatabase.getReference(REFERENCE_NOTIFICATIONS);
    }

    @Override
    public LiveData<User> getUser() {

        final MutableLiveData<User> user = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mUsersReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user.setValue(dataSnapshot.getValue(User.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else {
            // TODO: Not logged in...
            return null;
        }
        return user;
    }

    @Override
    public void updateUserType(int type) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mUsersReference.child(firebaseUser.getUid()).child(REFERENCE_USERS_CHILD_TYPE).setValue(type);
        }
    }

    public void refreshNotificationTokenId(String tokenId) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mUsersReference.child(firebaseUser.getUid())
                    .child(NOTIFICATION_TOKEN_ID).setValue(tokenId);
        }
    }

    @Override
    public void createUser(String userId, User user) {
        mUsersReference.child(userId).setValue(user);
    }

    @Override
    public LiveData<List<Drive>> getDrives() {
        final MutableLiveData<List<Drive>> drivesList = new MutableLiveData<>();
        final List<Drive> tempDrivesList = new ArrayList<>();

        mDrivesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    tempDrivesList.add(snapshot.getValue(Drive.class));
                }
                drivesList.setValue(tempDrivesList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return drivesList;
    }

    public LiveData<Drive> findBestRideMatch(final Position startLocation, final Position endLocation, final long time) {

        final MutableLiveData<Drive> bestDriveMatch = new MutableLiveData<>();

        mDrivesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                com.cybercom.passenger.repository.databasemodel.Drive bestMatch = null;
                float shortestDistance = 0;
                float[] distance = new float[2];

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    com.cybercom.passenger.repository.databasemodel.Drive drive = snapshot.getValue(com.cybercom.passenger.repository.databasemodel.Drive.class);

                    if (drive != null && Math.abs(time - drive.getTime()) < DRIVE_REQUEST_MATCH_TIME_THRESHOLD) {
                        Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                                drive.getStartLocation().getLatitude(), drive.getStartLocation().getLongitude(), distance);

                        Timber.d("Drives: distance: %s, driveRequest: lat: %s, lng: %s, drive: lat %s, lng %s",
                                distance[0], startLocation.getLatitude(), startLocation.getLongitude(),
                                drive.getStartLocation().getLatitude(), drive.getStartLocation().getLongitude());

                        if (distance[0] < 700) {
                            if (bestMatch == null) {
                                bestMatch = drive;
                                shortestDistance = distance[0];
                            } else if (distance[0] < shortestDistance) {
                                bestMatch = drive;
                                shortestDistance = distance[0];
                            }
                        }
                    } else {
                        Timber.d("Drives: Out of time frame!");
                    }
                }
                Timber.d("Drives: Best match:  distance: %s, Drive: %s", distance[0], bestMatch);


                if (bestMatch != null) {
                    final com.cybercom.passenger.repository.databasemodel.Drive finalBestMatch = bestMatch;

                    mUsersReference.child(finalBestMatch.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User driver = dataSnapshot.getValue(User.class);
                            bestDriveMatch.setValue(new Drive(driver, finalBestMatch.getTime(),
                                    finalBestMatch.getStartLocation(), finalBestMatch.getEndLocation(),
                                    finalBestMatch.getAvailableSeats()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    bestDriveMatch.setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return bestDriveMatch;
    }

    @Override
    public String addDrive(Drive drive) {
        com.cybercom.passenger.repository.databasemodel.Drive fDrive = DatabaseModelHelper.convertDrive(drive);

        DatabaseReference ref = mDrivesReference.push();
        ref.setValue(fDrive);

        return ref.getKey();
    }

    @Override
    public void addDriveRequest(DriveRequest driveRequest) {
        com.cybercom.passenger.repository.databasemodel.DriveRequest fDriveRequest =
                DatabaseModelHelper.convertDriveRequest(driveRequest);
        mDriveRequestsReference.push().setValue(fDriveRequest);
    }

    public void sendNotification(Notification notification) {
        com.cybercom.passenger.repository.databasemodel.Notification dataBaseNotification =
                DatabaseModelHelper.convertNotification(notification);

        mNotificationsReference.push().setValue(dataBaseNotification);
    }

    public LiveData<Notification> receiveIncomingNotifications() {
        return mNotification;
    }

    public void setIncomingNotification(final Map<String, String> payload) {
        mUsersReference.child(payload.get(DRIVE_DRIVER_ID))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User driver = dataSnapshot.getValue(User.class);
                        mUsersReference.child(payload.get(DRIVE_REQUEST_PASSENGER))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User passenger = dataSnapshot.getValue(User.class);

                                        addToNotificationQueue(DatabaseModelHelper.convertPayloadToNotification(payload, driver, passenger));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Timber.e("getPassenger:onCancelled",
                                                databaseError.toException());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.e("getDriver:onCancelled", databaseError.toException());
                    }
                });
    }

    private void addToNotificationQueue(Notification notification) {
        mNotificationQueue.add(notification);
        if (mNotification.getValue() == null) {
            mNotification.postValue(notification);
        }
    }

    public void removeNotification(Notification notification) {
        mNotificationQueue.remove(notification);
        mNotification.postValue(mNotificationQueue.poll());
    }
}
