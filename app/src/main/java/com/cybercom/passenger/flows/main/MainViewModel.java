package com.cybercom.passenger.flows.main;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

public class MainViewModel extends AndroidViewModel {

    private FusedLocationProviderClient mFusedLocationClient;
    private PassengerRepository mPassengerRepository = PassengerRepository.getInstance();
    private MutableLiveData<Location> mMyLocation = new MutableLiveData<>();
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LiveData<Notification> mIncomingNotification;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(application);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mMyLocation.setValue(location);
                }
            }
        };
        createLocationRequest();
    }

    public LiveData<Location> getUpdatedLocationLiveData() {
        return mMyLocation;
    }

    @SuppressWarnings("MissingPermission")
    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.myLooper());
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public Drive createDrive(User user, Position startLocation, Position endLocation) {
        Long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;

        Drive drive = new Drive(user, currentTimeMillis,startLocation,endLocation, seats);
        mPassengerRepository.addDrive(drive);

        return drive;
    }

    public DriveRequest createDriveRequest(User user, Position startLocation, Position endLocation) {
        Long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;

        DriveRequest driveRequest = new DriveRequest(user, currentTimeMillis, startLocation, endLocation, seats);
        mPassengerRepository.addDriveRequest(driveRequest);

        return driveRequest;
    }

    public LiveData<Drive> findBestDriveMatch(Position startLocation, Position endLocation) {
        long currentTimeMillis = System.currentTimeMillis();

        return mPassengerRepository.findBestRideMatch(startLocation, endLocation, currentTimeMillis);
    }

    public void addRequestDriveNotification(DriveRequest driveRequest, Drive drive) {

        Notification pendingPassengerNotification = new Notification(Notification.REQUEST_DRIVE, driveRequest, drive);
        mPassengerRepository.sendNotification(pendingPassengerNotification);
    }

    public LiveData<Notification> getIncomingNotifications() {
        if (mIncomingNotification == null) {
            mIncomingNotification = mPassengerRepository.receiveIncomingNotifications();
        }
        return mIncomingNotification;
    }

    public void sendAcceptPassengerNotification(Drive drive, DriveRequest driveRequest) {
        Notification acceptPassenger = new Notification(Notification.ACCEPT_PASSENGER, driveRequest, drive);
        mPassengerRepository.sendNotification(acceptPassenger);
    }

    public void sendRejectPassengerNotification(Drive drive, DriveRequest driveRequest) {
        Notification rejectPassenger = new Notification(Notification.REJECT_PASSENGER, driveRequest, drive);
        mPassengerRepository.sendNotification(rejectPassenger);

    }

    public void setIncomingNotification(Bundle extras) {

        Map<String, String> payload = new HashMap<>();

        for (String key : extras.keySet()) {
            payload.put(key, extras.getString(key));
        }

        mPassengerRepository.setIncomingNotification(payload);

    }

    public void pollNotificationQueue(Notification notification){
        mPassengerRepository.pollNotificationQueue(notification);
    }

    public void dismissNotification() {
        mPassengerRepository.dismissNotification();
    }

    public void refreshToken(String token) {
        mPassengerRepository.refreshNotificationTokenId(token);
    }

    public LiveData<User> getUser() {
        return mPassengerRepository.getUser();
    }
}
