package com.cybercom.passenger;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import timber.log.Timber;


public class MainViewModel extends AndroidViewModel {

    private FusedLocationProviderClient mFusedLocationClient;
    public Location mLastLocation;
    private PassengerRepository mPassengerRepository;

    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mFusedLocationClient = new FusedLocationProviderClient(application);
        mPassengerRepository = PassengerRepository.getInstance();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Get location update here
                    Timber.d("Get updated location: %f %f", location.getLatitude(), location.getLongitude());
                }
            }
        };

        createLocationRequest();
    }

    public void getLocation(){
        getLastLocation();
    }

    @SuppressWarnings("MissingPermission")
    public void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener( new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                        } else {
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

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
