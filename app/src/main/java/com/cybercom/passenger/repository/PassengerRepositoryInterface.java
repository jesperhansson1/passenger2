package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;

import com.cybercom.passenger.model.Car;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.User;

import java.util.List;

public interface PassengerRepositoryInterface {
    LiveData<User> getUser();

    void updateUserType(int type);

    void createUser(String userId, User user);

    LiveData<List<Drive>> getDrives();

    String addDrive(Drive drive);

    void addDriveRequest(DriveRequest driveRequest);

    void createCar(String carId, String userId, Car car);

    void removeCar(String carId, String userId);
}
