package com.cybercom.passenger.flows.main;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {

    private static final long FIND_MATCH_TIMEOUT_MS = 20 * 1000;
    public static final double LOWER_LEFT_LATITUDE = 55.0059799;
    public static final double LOWER_LEFT_LONGITUDE = 10.5798;
    public static final double UPPER_RIGHT_LATITUDE = 69.0599709;
    public static final double UPPER_RIGHT_LONGITUDE = 24.1773101;

    private FusedLocationProviderClient mFusedLocationClient;
    private PassengerRepository mPassengerRepository = PassengerRepository.getInstance();
    private MutableLiveData<Location> mMyLocation = new MutableLiveData<>();
    private Boolean isInitialZoomDone = false;
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

    @SuppressLint("MissingPermission")
    public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener){
        mFusedLocationClient.getLastLocation().addOnSuccessListener(onSuccessListener);
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public LiveData<Drive> createDrive(long time, Position startLocation, Position endLocation, int availableSeats) {
        return mPassengerRepository.createDrive(time, startLocation, endLocation, availableSeats);
    }

    public LiveData<DriveRequest> createDriveRequest(long time, Position startLocation, Position endLocation, int seats) {
        return mPassengerRepository.createDriveRequest(time, startLocation, endLocation, seats);
    }

    public LiveData<Drive> findBestDriveMatch(DriveRequest driveRequest) {
        return mPassengerRepository.findBestRideMatch(driveRequest);
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

//    TODO: Remove?
//    public DriveRequest updateDriveRequestBlacklist(Notification notification) {
//        DriveRequest driveRequest = notification.getDriveRequest();
//        Drive drive = notification.getDrive();
//
//        mPassengerRepository.updateDriveRequestBlacklist(driveRequest.getId(),
//                drive.getDriver().getUserId());
//
//        driveRequest.addDriverIdBlackList(drive.getDriver().getUserId());
//
//        return driveRequest;
//    }

    public void setIncomingNotification(Bundle extras) {

        Map<String, String> payload = new HashMap<>();

        for (String key : extras.keySet()) {
            payload.put(key, extras.getString(key));
        }

        mPassengerRepository.setIncomingNotification(payload);
    }

    public void pollNotificationQueue(Notification notification) {
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

    public LiveData<Boolean> setFindMatchTimer() {
        final MutableLiveData<Boolean> findMatchTimerLiveData = new MutableLiveData<>();

        new Handler(Looper.getMainLooper()).postDelayed((new Runnable() {
            @Override
            public void run() {
                Timber.d("findMatch timed out");
                findMatchTimerLiveData.setValue(true);
            }
        }), FIND_MATCH_TIMEOUT_MS);

        return findMatchTimerLiveData;
    }

    // CreateDriveFragment
    private int numberOfPassengers = 4;
    private MutableLiveData<String> mStartLocationAddress = new MutableLiveData<>();
    private MutableLiveData<Location> mStartMarkerLocation = new MutableLiveData<>();
    private MutableLiveData<Location> mEndMarkerLocation = new MutableLiveData<>();
    private MutableLiveData<String> mEndLocationAddress = new MutableLiveData<>();


    public void setNumberOfPassengers(int passengers) {
        numberOfPassengers = passengers;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    private String getAddressFromLocation(Location location) {
        List<Address> addresses;
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Timber.i(addresses.get(0).toString());
            return addresses.get(0).getThoroughfare() + " " + addresses.get(0).getFeatureName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Location getLocationFromAddress(String address) {
        List<Address> addresses;
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocationName(address, 1,
                    LOWER_LEFT_LATITUDE,
                    LOWER_LEFT_LONGITUDE,
                    UPPER_RIGHT_LATITUDE,
                    UPPER_RIGHT_LONGITUDE);

            if (addresses.size() != 0) {
                Location locationFromAddress = new Location("LocationFromAddress");
                locationFromAddress.setLatitude(addresses.get(0).getLatitude());
                locationFromAddress.setLongitude(addresses.get(0).getLongitude());

                return locationFromAddress;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<String> getStartLocationAddress() {
        return mStartLocationAddress;
    }

    public void setStartLocationAddress(String address) {
        mStartLocationAddress.setValue(address);
        mStartMarkerLocation.setValue(getLocationFromAddress(address));
    }

    public void setStartMarkerLocation(Location location) {
        mStartMarkerLocation.setValue(location);
        mStartLocationAddress.setValue(getAddressFromLocation(location));
    }

    public LiveData<Location> getStartMarkerLocation() {
        return mStartMarkerLocation;
    }

    public void setEndMarkerLocation(Location location) {
        mEndMarkerLocation.setValue(location);
        mEndLocationAddress.setValue(getAddressFromLocation(location));
    }

    public void setEndLocationAddress(String address){
        mEndLocationAddress.setValue(address);
        mEndMarkerLocation.setValue(getLocationFromAddress(address));
    }

    public LiveData<Location> getEndMarkerLocation() {
        return mEndMarkerLocation;
    }

    public LiveData<String> getEndLocationAddress() {
        return mEndLocationAddress;
    }

    public Boolean isInitialZoomDone() {
        return isInitialZoomDone;
    }

    public void setInitialZoomDone(Boolean initialZoomDone) {
        isInitialZoomDone = initialZoomDone;
    }
}
