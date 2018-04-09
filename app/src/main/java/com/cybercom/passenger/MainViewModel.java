package com.cybercom.passenger;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import android.os.Looper;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

public class MainViewModel extends AndroidViewModel {

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
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
    public void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.myLooper());
    }

    public Location getLastSeenLocation()
    {
        return mLastLocation;
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public Drive createDrive(Position startLocation, Position endLocation) {
        Long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;

        Drive drive = new Drive(PassengerRepository.gDriver, currentTimeMillis,startLocation,endLocation, seats);
        mPassengerRepository.addDrive(drive);

        return drive;
    }

    public DriveRequest createDriveRequest(Position startLocation, Position endLocation) {
        Long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;

        DriveRequest driveRequest = new DriveRequest(PassengerRepository.gPassenger, currentTimeMillis, startLocation, endLocation, seats);
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

    public void sendRejectPassengerNotificaiton() {
        // TODO: implement

    }
}
