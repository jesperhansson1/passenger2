package com.cybercom.passenger.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.repository.PassengerRepository;
import com.cybercom.passenger.repository.networking.DistantMatrixAPIHelper;
import com.cybercom.passenger.repository.networking.model.DistanceMatrixResponse;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ForegroundServices extends LifecycleService {

    public static final int TIME_BETWEEN_ETA_LOOKUPS_DELAY_MILLIS = 30 * 1000;
    private static final long FIRST_TIME_ETA_LOOKUP_DELAY_MILLIS = 2000;
    private PassengerRepository mPassengerRepository = PassengerRepository.getInstance();
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private MutableLiveData<Location> mMyLocationMutableLiveData = new MutableLiveData<>();
    private Location mCurrentLocation = new Location("");
    private static final int INTERVAL = 1000;
    private static final int FASTEST_INTERVAL = 1000;
    public static final String INTENT_EXTRA_DRIVE_ID = "driveId";
    public static final String INTENT_EXTRA_PASSENGER_RIDE_ID = "passengerRideId";
    private Position mPickUpLocation;
    private Position mDriversPosition;
    private boolean mPickUpConfirmed;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle extras = intent.getExtras();
        String driveId = extras.getString(INTENT_EXTRA_DRIVE_ID);
        String passengerRideId = extras.getString(INTENT_EXTRA_PASSENGER_RIDE_ID);
        //Uppdatera Drivers position
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_UPDATE_DRIVER_POSITION)) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    if (locationResult == null) {
                        return;
                    }

                    Location prevLocation;

                    int locationResultSize = locationResult.getLocations().size();
                    if (locationResultSize == 1) {
                        prevLocation = mCurrentLocation;
                    } else {
                        prevLocation = locationResult.getLocations().get(locationResultSize - 2);
                    }

                    for (Location location : locationResult.getLocations()) {
                        mMyLocationMutableLiveData.setValue(location);
                        Timber.d("DriveId %s", driveId);

                        mCurrentLocation = location;
                    }

                    float distanceDelta = mCurrentLocation.distanceTo(prevLocation);
                    float timeDelta = Math.abs(mCurrentLocation.getTime() - prevLocation.getTime()) / 1000f;
                    float speed = 1;

                    if (timeDelta != 0.0f) {
                        speed = distanceDelta / timeDelta;
                    }

                    Timber.d("currentSpeed: %f", speed);

                    mPassengerRepository.updateDriveCurrentLocation(driveId, mCurrentLocation);
                    mPassengerRepository.updateDriveCurrentVelocity(driveId, speed);
                }
            };

            createLocationRequest();
            startLocationUpdates();

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.foreground_notification);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this,
                    NotificationHelper.TRACKING_CHANNEL)
                    .setContentTitle(getString(R.string.passenger))
                    .setTicker(getString(R.string.passenger))
                    .setContentText(getString(R.string.passenger))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(notificationView)
                    .setOngoing(true).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }

        //Uppdatera Passengers position
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_UPDATE_PASSENGER_POSITION)) {

            PassengerRepository.getInstance().getDriverPosition(driveId).observe(this,
                    position -> mDriversPosition = position);

            mPassengerRepository.getPassengerRideById(passengerRideId).observe(this,
                    passengerRide -> {
                if (passengerRide == null) return;
                mPickUpLocation = passengerRide.getPickUpPosition();
                mPickUpConfirmed = passengerRide.isPickUpConfirmed();
            });

            new Handler().postDelayed(this::calculateETAToPickUpLocation,
                    FIRST_TIME_ETA_LOOKUP_DELAY_MILLIS);

            startScheduledETACalculation();

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        mMyLocationMutableLiveData.setValue(location);

                        mCurrentLocation = location;
                    }
                    mPassengerRepository.updatePassengerRideCurrentLocation(mCurrentLocation);
                }
            };
            createLocationRequest();
            startLocationUpdates();

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.foreground_notification);

            Notification notification = new NotificationCompat.Builder(this,
                    NotificationHelper.TRACKING_CHANNEL)
                    .setContentTitle(getString(R.string.passenger))
                    .setTicker(getString(R.string.passenger))
                    .setContentText(getString(R.string.passenger))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(notificationView)
                    .setOngoing(true).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }

        //Get Driver Position
        //Get Passenger Position

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND)) {
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND)) {

            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void startScheduledETACalculation() {
        new Handler().postDelayed(() -> {
            calculateETAToPickUpLocation();

            if (mPickUpConfirmed) {
                return;
            }

            // Re-run this until passenger is picked up or is dismissed or cancels
            startScheduledETACalculation();
        }, TIME_BETWEEN_ETA_LOOKUPS_DELAY_MILLIS);
    }

    private void calculateETAToPickUpLocation() {
        if (mDriversPosition == null | mPickUpLocation == null) {
            return;
        }

        Timber.d("mDriverPos %s , %S", mDriversPosition.getLatitude(), mDriversPosition.getLongitude());
        Timber.d("mPickUpLocation %s , %S", mPickUpLocation.getLatitude(), mPickUpLocation.getLongitude());

        DistantMatrixAPIHelper.getInstance().mMatrixAPIService.getDistantMatrix(
                LocationHelper.getStringFromLatLng(
                        mDriversPosition.getLatitude(), mDriversPosition.getLongitude()),
                LocationHelper.getStringFromLatLng(
                        mPickUpLocation.getLatitude(), mPickUpLocation.getLongitude()),
                getResources().getString(R.string.google_api_key)).enqueue(new Callback<DistanceMatrixResponse>() {
            @Override
            public void onResponse(Call<DistanceMatrixResponse> call, Response<DistanceMatrixResponse> response) {
                if (response.isSuccessful()) {
                    long duration = DistantMatrixAPIHelper.getDurationFromResponse(response, 0, 0);
//                    long dis = DistantMatrixAPIHelper.getDistanceFromResponse(response, 0, 0);
                    Timber.d("eta: %s", duration);
                    mPassengerRepository.updateETA(duration);
                }
            }

            @Override
            public void onFailure(Call<DistanceMatrixResponse> call, Throwable t) {
            }
        });

    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public LiveData<Location> getUpdatedLocationLiveData() {
        return mMyLocationMutableLiveData;
    }

    @SuppressWarnings("MissingPermission")
    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.myLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        super.onBind(intent);
        return null;
    }

}
