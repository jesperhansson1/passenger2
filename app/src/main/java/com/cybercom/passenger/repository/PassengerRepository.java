package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class PassengerRepository implements PassengerRepositoryInterface {

    FirebaseAuth auth;

    private static final String REFERENCE_USERS = "users";
    private static final String REFERENCE_DRIVES = "drives";
    private static final String REFERENCE_DRIVE_REQUESTS = "driveRequests";
    private static final String REFERENCE_USERS_CHILD_TYPE = "type";
    private static final String MOCK_USER = "userone";

    private static final int DRIVE_REQUEST_MATCH_TIME_THRESHOLD = 15 * 60 * 60 * 1000;

    private static PassengerRepository sPassengerRepository;
    private DatabaseReference mUsersReference;
    private DatabaseReference mDrivesReference;
    private DatabaseReference mDriveRequestsReference;

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
    }

    @Override
    public LiveData<User> getUser() {

        final MutableLiveData<User> user = new MutableLiveData<>();

        mUsersReference.child(MOCK_USER).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user.setValue(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return user;
    }

    @Override
    public void updateUserType(int type) {
        mUsersReference.child(MOCK_USER).child(REFERENCE_USERS_CHILD_TYPE).setValue(type);
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

        final MutableLiveData<Drive> bestDriveMatch  = new MutableLiveData<>();

        mDrivesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Drive bestMatch = null;
                float shortestDistance = 0;
                float[] distance = new float[2];

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Drive drive = snapshot.getValue(Drive.class);

                    if (drive != null && Math.abs(driveRequest.getTime() - drive.getTime()) < DRIVE_REQUEST_MATCH_TIME_THRESHOLD) {
                            Location.distanceBetween(driveRequest.getStartLocation().getLatitude(), driveRequest.getStartLocation().getLongitude(),
                                    drive.getStartLocation().getLatitude(), drive.getStartLocation().getLongitude(), distance);

                        Timber.d("Drives: distance: %s, driveRequest: lat: %s, lng: %s, drive: lat %s, lng %s",
                                distance[0], driveRequest.getStartLocation().getLatitude(), driveRequest.getStartLocation().getLongitude(),
                                drive.getStartLocation().getLatitude(), drive.getStartLocation().getLongitude());

                            if(distance[0] < 700){
                                if (bestMatch == null) {
                                    bestMatch = drive;
                                    shortestDistance = distance[0];
                                }
                                else if(distance[0] < shortestDistance){
                                    bestMatch = drive;
                                    shortestDistance = distance[0];
                                }
                            }
                    } else{
                        Timber.d("Drives: Out of time frame!");
                    }
                }
                Timber.d("Drives: Best match:  distance: %s, Drive: %s", distance[0], bestMatch);
                bestDriveMatch.setValue(bestMatch);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return bestDriveMatch;
    }

    @Override
    public void addDrive(Drive drive) {
        mDrivesReference.child(generateRandomUUID()).setValue(drive);
    }

    @Override
    public void addDriveRequest(DriveRequest driveRequest) {
        mDriveRequestsReference.child(generateRandomUUID()).setValue(driveRequest);
    }

    private String generateRandomUUID(){
        return UUID.randomUUID().toString();
    }
}
