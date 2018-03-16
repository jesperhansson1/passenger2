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
import java.util.UUID;

public class PassengerRepository implements PassengerRepositoryInterface {

    private static final String USERS_REFERENCE = "users";
    private static final String USER_CHILD_TYPE = "type";
    private static final String MOCK_USER = "userone";

    private static PassengerRepository sPassengerRepository;
    private DatabaseReference mUserReference;

    public static PassengerRepository getInstance(){
        if (sPassengerRepository == null) {
            sPassengerRepository = new PassengerRepository();
        }
        return sPassengerRepository;
    }

    private PassengerRepository() {
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = mFirebaseDatabase.getReference(USERS_REFERENCE);
    }

    @Override
    public LiveData<User> getUser() {

        final MutableLiveData<User> user = new MutableLiveData<>();

         mUserReference.child(MOCK_USER).addValueEventListener(new ValueEventListener() {
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
        mUserReference.child(MOCK_USER).child(USER_CHILD_TYPE).setValue(type);
    }

    @Override
    public void createUser(User user) {
        mUserReference.child(MOCK_USER).setValue(user);
    }

    @Override
    public LiveData<List<Drive>> getDrives() {

        
        return null;
    }

    @Override
    public void addDrive(Drive drive) {

    }

    @Override
    public void addDriveRequest(DriveRequest driveRequest) {

    }
}
