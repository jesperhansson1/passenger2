package com.cybercom.passenger.repository;

import android.arch.lifecycle.LiveData;
import android.location.Location;

import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.model.Car;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;

import java.util.List;
import java.util.Map;

interface PassengerRepositoryInterface {
    LiveData<User> getUser();

    void updateUserType(int type);

  //  void createUser(String userId, User user);

    void sendNotification(Notification notification);

    LiveData<Notification> receiveIncomingNotifications();

    void setIncomingNotification(final Map<String, String> payload);

    LiveData<Drive> createDrive(long time, Position startLocation, Position endLocation, int availableSeats, Bounds bounds);

    LiveData<DriveRequest> createDriveRequest(long time, Position startLocation, Position endLocation, int availableSeats, double price);

    LiveData<Drive> findBestRideMatch(final DriveRequest driveRequest, int radiusMultiplier, String googleApiKey);

    void createCar(String carId, String userId, Car car);

    void removeCar(String carId, String userId);

    void updateDriveCurrentLocation(String driverId, Location location);

}
