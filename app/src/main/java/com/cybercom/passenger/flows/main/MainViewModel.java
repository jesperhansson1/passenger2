package com.cybercom.passenger.flows.main;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.PassengerRide;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {

    private static final long FIND_MATCH_TIMEOUT_MS = 2 * 60 * 1000;
    public static final double LOWER_LEFT_LATITUDE = 55.0059799;
    public static final double LOWER_LEFT_LONGITUDE = 10.5798;
    public static final double UPPER_RIGHT_LATITUDE = 69.0599709;
    public static final double UPPER_RIGHT_LONGITUDE = 24.1773101;
    private static final int INTERVAL = 1000;
    private static final int FASTEST_INTERVAL = 1000;

    public static final int DRIVE_REQUEST_DEFAULT_MULTIPLIER = 1;
    public static final int DRIVE_REQUEST_INCREASE_MULTIPLIER_BY_ONE = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    private PassengerRepository mPassengerRepository = PassengerRepository.getInstance();
    private MutableLiveData<Location> mMyLocation = new MutableLiveData<>();
    private Boolean isInitialZoomDone = false;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LiveData<Notification> mIncomingNotification;
    private DriveRequest mMostRecentDriveRequest;

    private int mDriveRequestRadiusMultiplier;

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
    public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener) {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(onSuccessListener);
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public LiveData<Drive> createDrive(long time, Position startLocation, Position endLocation,
                                       int availableSeats, Bounds bounds) {


        return mPassengerRepository.createDrive(time, startLocation, endLocation, availableSeats, bounds);
    }

    public void removeCurrentDrive(String driveId, OnCompleteListener onCompleteListener) {
        mPassengerRepository.removeCurrentDrive(driveId, onCompleteListener);
    }

    public void removePassengerRide(String passengerId, OnCompleteListener onCompleteListener) {
        mPassengerRepository.removePassengerRide(passengerId, onCompleteListener);
    }

    public LiveData<DriveRequest> createDriveRequest(long time, Position startLocation, Position endLocation, int seats, double price, String chargeId) {
        return mPassengerRepository.createDriveRequest(time, startLocation, endLocation, seats, price, chargeId);
    }

    public LiveData<Drive> findBestDriveMatch(DriveRequest driveRequest, int radiusMultiplier, String googleApiKey) {
        mMostRecentDriveRequest = driveRequest;
        return mPassengerRepository.findBestRideMatch(driveRequest, radiusMultiplier, googleApiKey);
    }

    public void cancelDriveMatch() {
        mPassengerRepository.cancelBestRideMatch();
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

    public void getNextNotification() {
        mPassengerRepository.getNextNotification();
    }

    public void refreshToken(String token) {
        mPassengerRepository.refreshNotificationTokenId(token);
    }

    public LiveData<User> getUser() {
        return mPassengerRepository.getUser();
    }

    public void updateUserType(int type) {
        mPassengerRepository.updateUserType(type);
    }

    public LiveData<Boolean> setFindMatchTimer() {
        final MutableLiveData<Boolean> findMatchTimerLiveData = new MutableLiveData<>();

        new Handler(Looper.getMainLooper()).postDelayed((() -> {
            Timber.d("findMatch timed out");
            findMatchTimerLiveData.setValue(true);
        }), FIND_MATCH_TIMEOUT_MS);

        return findMatchTimerLiveData;
    }

    // CreateDriveFragment
    public static final int PLACE_START_MARKER = 0;
    public static final int PLACE_END_MARKER = 1;
    private int mWhichMarkerToAdd = 0;
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

    public String getAddressFromLocation(Location location) {
        if (location == null) {
            return null;
        }
        List<Address> addresses;
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() != 0) {
                Timber.i(addresses.get(0).toString());
                return addresses.get(0).getAddressLine(0).split(",")[0];
            }

            return "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Location getLocationFromAddress(String address) {
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

    public void setEndLocationAddress(String address) {
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

    public int getWhichMarkerToAdd() {
        return mWhichMarkerToAdd;
    }

    public void setWhichMarkerToAdd(int whichMarkerToAdd) {
        mWhichMarkerToAdd = whichMarkerToAdd;
    }

    public void setEndMarker(MutableLiveData<Location> endMarker) {
        mEndMarkerLocation = endMarker;
    }

    public void setEndAddress(MutableLiveData<String> endAddress) {
        mEndLocationAddress = endAddress;
    }

    @SuppressLint("MissingPermission")
    public void setCurrentLocationToDrive(String driveId, Location location) {
        mPassengerRepository.updateDriveCurrentLocation(driveId, location);
    }

    public MutableLiveData<Location> getDriverCurrentLocation() {
        return mPassengerRepository.getDriverCurrentLocation();
    }

    @SuppressLint("MissingPermission")
    public LiveData<PassengerRide> createPassengerRide(Drive drive, Position startPosition,
                                                       Position endPosition, String startAddress,
                                                       String endAddress, String chargeId,
                                                       double price) {
        return mPassengerRepository.createPassengerRide(drive, startPosition, endPosition,
                startAddress, endAddress, chargeId, price);
    }

    public LiveData<PassengerRide> getPassengerRides(String driveId) {
        return mPassengerRepository.getPassengerRides(driveId);
    }

    public LiveData<Position> getDriverPosition(String driveId) {
        return mPassengerRepository.getDriverPosition(driveId);
    }

    public DriveRequest getMostRecentDriveRequest() {
        return mMostRecentDriveRequest;
    }

    public int getDriveRequestRadiusMultiplier() {
        return mDriveRequestRadiusMultiplier;
    }

    public void setDriveRequestRadiusMultiplier(int mDriveRequestRadiusMultiplier) {
        this.mDriveRequestRadiusMultiplier = mDriveRequestRadiusMultiplier;
    }

    public LiveData<Position> getPassengerPosition(String driveId) {
        return mPassengerRepository.getPassengerPosition(driveId);
    }

    public LiveData<Drive> getActiveDrive() {
        return mPassengerRepository.getActiveDrive();
    }

    public LiveData<PassengerRide> getActivePassengerRide() {
        return mPassengerRepository.getActivePassengerRide();
    }

    public LiveData<Integer> getETA() {
        return mPassengerRepository.getETAInMin();
    }

    public Drive getMatchedDrive() {
        return mPassengerRepository.getMatchedDrive();
    }

    public void confirmPickUp(PassengerRide passengerRide) {
        mPassengerRepository.confirmPickUp(passengerRide.getId());
    }

    public void cancelPassengerRide(String passengerRideId) {
        mPassengerRepository.cancelPassengerRide(passengerRideId);
    }

    public void confirmPickUp(String driveId) {
        mPassengerRepository.passengerConfirmPickUp(driveId);
    }

    public void confirmDropOff(PassengerRide passengerRide) {
        mPassengerRepository.confirmDropOff(passengerRide.getId());
    }

    public LiveData<com.cybercom.passenger.repository.databasemodel.PassengerRide>
        getPassengerRideById(String passengerRideId) {
        return mPassengerRepository.getPassengerRideById(passengerRideId);
    }

    public String getChargeId(Drive drive, String passengerId) {
        return mPassengerRepository.getChargeIdForRefund(drive,passengerId);
    }

    public void refundFull(String chargeId){
        mPassengerRepository.refundFull(chargeId);
    }

    //charge no show fee for passenger customer and refund remaining amount
    public  void noShowPassenger(String chargeId, String customerId){
        mPassengerRepository.transferRefund(chargeId, customerId);
    }

    public Uri getImageUri(String userId)
    {
        return mPassengerRepository.getImageUri(userId);
    }

}
