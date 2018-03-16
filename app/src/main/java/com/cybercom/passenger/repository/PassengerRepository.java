package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class PassengerRepository implements PassengerRepositoryInterface {

    private static final String REFERENCE_USERS = "users";
    private static final String USER_CHILD_TYPE = "type";
    private static final String MOCK_USER = "userone";
    private static final String MOCK_DRIVE = "driveone";
    private static final String REFERENCE_DRIVES = "drives";

    private static PassengerRepository sPassengerRepository;
    private DatabaseReference mUsersReference;
    private DatabaseReference mDrivesReference;

    public static PassengerRepository getInstance(){
        if (sPassengerRepository == null) {
            sPassengerRepository = new PassengerRepository();
        }
        return sPassengerRepository;
    }

    private PassengerRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = firebaseDatabase.getReference(REFERENCE_USERS);
        mDrivesReference = firebaseDatabase.getReference(REFERENCE_DRIVES);
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
        mUsersReference.child(MOCK_USER).child(USER_CHILD_TYPE).setValue(type);
    }

    @Override
    public void createUser(User user) {
        mUsersReference.child(MOCK_USER).setValue(user);
    }

    @Override
    public LiveData<List<Drive>> getDrives() {


        return null;
    }

    @Override
    public void addDrive(Drive drive) {
        mDrivesReference.child(MOCK_DRIVE).setValue(drive);
    }

    @Override
    public void addDriveRequest(DriveRequest driveRequest) {

    }
}
