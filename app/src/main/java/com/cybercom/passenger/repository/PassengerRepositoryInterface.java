package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.User;

import java.util.List;

public interface PassengerRepositoryInterface {
    LiveData<User> getUser();

    void updateUserType(int type);

    void createUser(User user);

    LiveData<List<Drive>> getDrives();

    void addDrive(Drive drive);

    void addDriveRequest(DriveRequest driveRequest);
}
