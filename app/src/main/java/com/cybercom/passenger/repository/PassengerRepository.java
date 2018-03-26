package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
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

    private static final String REFERENCE_USERS = "users";
    private static final String REFERENCE_DRIVES = "drives";
    private static final String REFERENCE_DRIVE_REQUESTS = "driveRequests";
    private static final String REFERENCE_USERS_CHILD_TYPE = "type";
    private static final String MOCK_USER = "userone";

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
    public void createUser(User user) {
        mUsersReference.child(MOCK_USER).setValue(user);
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

    public Drive findBestRideMatch(final DriveRequest driveRequest) {
        final DriveRequest getDriveRequest = new DriveRequest(driveRequest.getTime(), driveRequest.getStartLocation(),
                driveRequest.getEndLocation(), driveRequest.getNotificationTokenId(), driveRequest.getExtraPassengers());

       final ArrayList getTime = new ArrayList<>();
       final ArrayList<Position> getStartLocation = new ArrayList();
       final ArrayList<Position> getEndLocation = new ArrayList();

       final ArrayList filterTimeDrives = new ArrayList<>();
       final ArrayList<Position> filterStartLocation = new ArrayList();
       final ArrayList<Position> filtertEndLocation = new ArrayList();
        final ArrayList sortDistanceBetween = new ArrayList<>();




        mDrivesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Drive bestMatch = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Drive drive = snapshot.getValue(Drive.class);

                    /*
                    if (getDriveRequest != null && Math.abs( getDriveRequest.getTime() - Long.parseLong(getTime.get(i).toString())) < (60 * 60 * 1000)) {
                        if (bestMatch == null) {
                            bestMatch = drive;
                            // räkna ut straäckn till startLocation ( DriveRequets.startLocation , drive.startLocation)
                            // long shortestDistance = ...;
                        }

                        // Räkna ut sträckan mellan startpunkterna jämför med shortsDistance

                        // om kortare sätt ny bestMatch = drive
                        // samt shortestDistance = ny sträcka


                        getTime.add(drive.getTime());
                    }
                    getStartLocation.add(drive.getStartLocation());
                    */
                }

                    for (int i = 0; i < getTime.size(); i++) {

                        if (getDriveRequest != null && Math.abs( getDriveRequest.getTime() - Long.parseLong(getTime.get(i).toString())) < (60 * 60 * 1000)) {
                            filterTimeDrives.add(getTime.get(i));
                            filterStartLocation.add(getStartLocation.get(i));
                        }
                    }

                if(filterTimeDrives.size() == 1){
                    Timber.d("HELLOOO one drive");
                    //if there is only one drive, send to viewmodel
                } else if (filterTimeDrives.size() == 0){
                    Timber.d("HELLOOO No drive matched!");
                } else {
                    Timber.d("HELLOOO More than one drive");
                    //if there are more than one, calculate the distance!
                    float[] distance = new float[filterTimeDrives.size()];

                    for(int i = 0; i < filterStartLocation.size(); i++){
                        Location.distanceBetween(driveRequest.getStartLocation().getLatitude(), driveRequest.getStartLocation().getLongitude(),
                                filterStartLocation.get(i).getLatitude(), filterStartLocation.get(i).getLongitude(), distance);
                        sortDistanceBetween.add(distance[i]);
                    }
                    Collections.sort(sortDistanceBetween);
                    System.out.println("dist " + sortDistanceBetween);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return null;
    }

    @Override
    public void addDrive(Drive drive) {
        mDrivesReference.child(generateRandomUUID()).setValue(drive);
    }

    @Override
    public void addDriveRequest(DriveRequest driveRequest) {
        findBestRideMatch(driveRequest);
        mDriveRequestsReference.child(generateRandomUUID()).setValue(driveRequest);
    }

    private String generateRandomUUID(){
        return UUID.randomUUID().toString();
    }
}
