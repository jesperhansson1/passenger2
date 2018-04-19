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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;

public class PassengerRepository implements PassengerRepositoryInterface {

    private static final String NOTIFICATION_TOKEN_ID = "notificationTokenId";
    private static final String REFERENCE_NOTIFICATIONS = "notifications";

    private static final String REFERENCE_USERS = "users";
    private static final String REFERENCE_DRIVES = "drives";
    private static final String REFERENCE_DRIVE_REQUESTS = "driveRequests";
    private static final String REFERENCE_USERS_CHILD_TYPE = "type";
    private static final String REFERENCE_DRIVER_ID_BLACK_LIST = "driverIdBlackList";

    private static final String DRIVE_DRIVER_ID = "driveDriverId";

    private static final int DRIVE_REQUEST_MATCH_TIME_THRESHOLD = 15 * 60 * 60 * 1000;
    private static final String NOTIFICATION_TYPE_KEY = "type";
    private static final String KEY_PAYLOAD_DRIVE_REQUEST_ID = "driveRequest";
    private static final String KEY_PAYLOAD_DRIVE_ID = "driveId";

    private static PassengerRepository sPassengerRepository;
    private DatabaseReference mUsersReference;
    private DatabaseReference mDrivesReference;
    private DatabaseReference mDriveRequestsReference;
    private DatabaseReference mNotificationsReference;

    private BlockingQueue<Notification> mNotificationQueue = new LinkedBlockingQueue<>();

    private MutableLiveData<Notification> mNotification = new MutableLiveData<>();
    private User mCurrentlyLoggedInUser;

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
                mCurrentlyLoggedInUser = dataSnapshot.getValue(User.class);
                user.setValue(mCurrentlyLoggedInUser);
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

    public LiveData<Drive> findBestRideMatch(final DriveRequest driveRequest) {

        final MutableLiveData<Drive> bestDriveMatch = new MutableLiveData<>();

        mDrivesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                com.cybercom.passenger.repository.databasemodel.Drive bestMatch = null;
                float shortestDistance = 0;
                String bestMatchDriveId = "";
                float[] distance = new float[2];

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    com.cybercom.passenger.repository.databasemodel.Drive drive = snapshot.getValue(com.cybercom.passenger.repository.databasemodel.Drive.class);

                    if (drive != null && Math.abs(driveRequest.getTime() - drive.getTime()) < DRIVE_REQUEST_MATCH_TIME_THRESHOLD) {
                        Location.distanceBetween(driveRequest.getStartLocation().getLatitude(), driveRequest.getStartLocation().getLongitude(),
                                drive.getStartLocation().getLatitude(), drive.getStartLocation().getLongitude(), distance);

                        Timber.d("Drives: distance: %s, driveRequest: lat: %s, lng: %s, drive: lat %s, lng %s",
                                distance[0], driveRequest.getStartLocation().getLatitude(), driveRequest.getEndLocation().getLongitude(),
                                drive.getStartLocation().getLatitude(), drive.getStartLocation().getLongitude());

                        if (driveRequest.getDriverIdBlackList().contains(drive.getDriverId())) Timber.i("No match, driver blacklisted: %s", drive.getDriverId());

                        if (distance[0] < 700 && !driveRequest.getDriverIdBlackList().contains(drive.getDriverId())) {
                            if (bestMatch == null) {
                                bestMatch = drive;
                                bestMatchDriveId = snapshot.getKey();
                                shortestDistance = distance[0];
                            } else if (distance[0] < shortestDistance) {
                                bestMatch = drive;
                                bestMatchDriveId = snapshot.getKey();
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
                    final String finalBestMatchDriveId = bestMatchDriveId;

                    mUsersReference.child(finalBestMatch.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User driver = dataSnapshot.getValue(User.class);
                            bestDriveMatch.setValue(new Drive(finalBestMatchDriveId, driver, finalBestMatch.getTime(),
                                    finalBestMatch.getStartLocation(), finalBestMatch.getEndLocation(),
                                    finalBestMatch.getAvailableSeats() ));
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

    public void sendNotification(Notification notification) {
        com.cybercom.passenger.repository.databasemodel.Notification dataBaseNotification =
                DatabaseModelHelper.convertNotification(notification);

        mNotificationsReference.push().setValue(dataBaseNotification);
    }

    public LiveData<Notification> receiveIncomingNotifications() {
        return mNotification;
    }

    /**
     * Rebuild the notification (to the ModelView model) from payload coming from push-notification
     * and add it to the notification queue
     *
     * @param payload The push-notification's payload
     */
    public void setIncomingNotification(final Map<String, String> payload) {

        final String driveId = payload.get(KEY_PAYLOAD_DRIVE_ID);
        final String driveRequestId = payload.get(KEY_PAYLOAD_DRIVE_REQUEST_ID);

        Timber.i("notification recevied %s", payload.toString());


        // Fetch the Drive
        mDrivesReference.child(driveId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final com.cybercom.passenger.repository.databasemodel.Drive dBdrive =
                                dataSnapshot.getValue(com.cybercom.passenger.repository.databasemodel.Drive.class);

                        // Fetch the Driver (User)
                        mUsersReference.child(dBdrive.getDriverId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final User driver = dataSnapshot.getValue(User.class);
                                        final String driverId = dataSnapshot.getKey();

                                        // Fetch the DriverRequest
                                        Timber.i("fetch driveRequest : %s", driveRequestId);
                                        mDriveRequestsReference.child(driveRequestId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final com.cybercom.passenger.repository.databasemodel.DriveRequest dBdriveRequest
                                                                = dataSnapshot.getValue(com.cybercom.passenger.repository.databasemodel.DriveRequest.class);

                                                        GenericTypeIndicator<List<String>> genTypeIndicatore = new GenericTypeIndicator<List<String>>() {};
                                                        List<String> list = dataSnapshot.child(REFERENCE_DRIVER_ID_BLACK_LIST).getValue(genTypeIndicatore);
                                                        if (list == null) {
                                                            list = new ArrayList<>();
                                                        }

                                                        // If it is a Reject-notification add driverId to the driver-request's blacklist
                                                        if (Integer.valueOf(payload.get(NOTIFICATION_TYPE_KEY)) == Notification.REJECT_PASSENGER) {
                                                            list.add(driver.getUserId());
                                                            List<Object> objectList = new ArrayList<Object>(list);
                                                            dBdriveRequest.setDriverIdBlackList(objectList);
                                                            mDriveRequestsReference.child(driveRequestId).setValue(dBdriveRequest);
                                                        } else {
                                                            dBdriveRequest.setDriverIdBlackList(new ArrayList<Object>(list));
                                                        }

                                                        // Fetch the passenger (User)
                                                        mUsersReference.child(dBdriveRequest.getPassengerId())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        User passenger = dataSnapshot.getValue(User.class);

                                                                        Drive drive = new Drive(driveId, driver,
                                                                                dBdrive.getTime(), dBdrive.getStartLocation(),
                                                                                dBdrive.getEndLocation(), dBdrive.getAvailableSeats());
                                                                        DriveRequest driveRequest = new DriveRequest(driveRequestId,
                                                                                passenger, dBdriveRequest.getTime(), dBdriveRequest.getStartLocation(),
                                                                                dBdriveRequest.getEndLocation(), dBdriveRequest.getExtraPassengers(),
                                                                                dBdriveRequest.getDriverIdBlackList());
                                                                        addToNotificationQueue(new Notification(Integer.parseInt(payload.get(NOTIFICATION_TYPE_KEY)),
                                                                                driveRequest, drive));
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                        Timber.i("Failed to fetch user: passenger: %s",
                                                                                databaseError.toString());
                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Timber.i("Failed to fetch DriveRequest: %s"
                                                                , databaseError.toString());
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Timber.i("Failed to fetch user: driver: %s"
                                                , databaseError.toString());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.i("Failed to fetch Drive %s", databaseError.toString());
                    }
                });
    }

    private void addToNotificationQueue(Notification notification) {
        mNotificationQueue.add(notification);
        if (mNotification.getValue() == null) {
            mNotification.postValue(mNotificationQueue.poll());
        }
    }

    public void pollNotificationQueue(Notification notification) {
        mNotificationQueue.remove(notification);
        mNotification.postValue(mNotificationQueue.poll());
    }

    public void dismissNotification() {
        mNotification.postValue(null);
    }

    public LiveData<Drive> createDrive(long time, Position startLocation, Position endLocation, int availableSeats) {
        final MutableLiveData<Drive> driveMutableLiveData = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uId = firebaseUser.getUid();

            final com.cybercom.passenger.repository.databasemodel.Drive dbDrive =
                new com.cybercom.passenger.repository.databasemodel.Drive(uId, time, startLocation, endLocation, availableSeats);
            final DatabaseReference ref = mDrivesReference.push();
            final String driveId = ref.getKey();
            ref.setValue(dbDrive);

            mUsersReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Drive drive = new Drive(driveId, user, dbDrive.getTime(),
                            dbDrive.getStartLocation(), dbDrive.getEndLocation(), dbDrive.getAvailableSeats());
                    driveMutableLiveData.setValue(drive);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } else {
            // Not logged in
            driveMutableLiveData.setValue(null);
        }
        return driveMutableLiveData;
    }

    public LiveData<DriveRequest> createDriveRequest(long time, Position startLocation, Position endLocation, int availableSeats) {
        final MutableLiveData<DriveRequest> driveRequestMutableLiveData = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uId = firebaseUser.getUid();

            final com.cybercom.passenger.repository.databasemodel.DriveRequest dbDriveRequest =
                new com.cybercom.passenger.repository.databasemodel.DriveRequest(uId, time, startLocation, endLocation, availableSeats, new ArrayList<Object>());
            final DatabaseReference ref = mDriveRequestsReference.push();
            final String driveRequestId = ref.getKey();
            ref.setValue(dbDriveRequest);

            mUsersReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    DriveRequest driveRequest = new DriveRequest(driveRequestId, user, dbDriveRequest.getTime(),
                            dbDriveRequest.getStartLocation(), dbDriveRequest.getEndLocation(),
                            dbDriveRequest.getExtraPassengers(), dbDriveRequest.getDriverIdBlackList());

                    driveRequestMutableLiveData.setValue(driveRequest);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } else {
            // Not logged in
            driveRequestMutableLiveData.setValue(null);
        }
        return driveRequestMutableLiveData;
    }

//    TODO: Remove?
//    public void updateDriveRequestBlacklist(String driveRequestId, String blackListDriverId) {
//        Timber.i("udpate driverRequest %s ", driveRequestId);
//        mDriveRequestsReference.child(driveRequestId).child(REFERENCE_DRIVER_ID_BLACK_LIST).push().setValue(blackListDriverId);
//
//    }

}
