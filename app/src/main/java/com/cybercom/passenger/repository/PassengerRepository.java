package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.User;

import java.util.List;

public class PassengerRepository implements PassengerRepositoryInterface {

    private static PassengerRepository sPassengerRepository;

    public static PassengerRepository getInstance(){
        if (sPassengerRepository == null) {
            sPassengerRepository = new PassengerRepository();
        }
        return sPassengerRepository;
    }

    private PassengerRepository() {}

    @Override
    public LiveData<User> getUser() {
        return null;
    }

    @Override
    public void updateUserType(int type) {

    }

    @Override
    public void createUser(User user) {

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
