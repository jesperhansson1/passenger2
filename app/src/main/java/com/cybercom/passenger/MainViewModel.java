package com.cybercom.passenger;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import timber.log.Timber;


public class MainViewModel extends AndroidViewModel {

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private PassengerRepository mPassengerRepository;
    private MutableLiveData<List<Drive>> list = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        mFusedLocationClient = new FusedLocationProviderClient(application);
        mPassengerRepository = PassengerRepository.getInstance();
    }

    public void getLocation(){
        System.out.println("Permission has already been accepted");
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
                            Timber.d("permission coord: %d %d", mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        } else {
                            Log.w("TAG", "permission getLastLocation:exception", task.getException());
                        }
                    }
                });
    }
}
