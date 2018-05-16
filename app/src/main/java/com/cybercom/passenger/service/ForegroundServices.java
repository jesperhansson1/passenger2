package com.cybercom.passenger.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import timber.log.Timber;

public class ForegroundServices extends Service {

    private PassengerRepository mPassengerRepository = PassengerRepository.getInstance();
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private MutableLiveData<Location> mMyLocation = new MutableLiveData<>();
    Location loc = new Location("");
    private static final int INTERVAL = 1000;
    private static final int FASTEST_INTERVAL = 1000;
    private static final String DRIVE_ID = "driveId";
    private static final String PASSENGER_RIDE_KEY = "passengerRideKey";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Uppdatera Drivers position
        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_UPDATE_DRIVER_POSITION)){

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        mMyLocation.setValue(location);

                        Bundle extras = intent.getExtras();
                        String driveId = extras.getString(DRIVE_ID);

                        Timber.d("DriveId %s", driveId);

                        loc.setLatitude(mMyLocation.getValue().getLatitude());
                        loc.setLongitude(mMyLocation.getValue().getLongitude());
                        mPassengerRepository.updateDriveCurrentLocation(driveId, loc);
                    }
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

            RemoteViews notificationView = new RemoteViews(this.getPackageName(),R.layout.foreground_notification);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.passenger))
                    .setTicker(getString(R.string.passenger))
                    .setContentText(getString(R.string.passenger))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContent(notificationView)
                    .setOngoing(true).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }

        //Uppdatera Passengers position
        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_UPDATE_PASSENGER_POSITION)){

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        mMyLocation.setValue(location);

                        Bundle extras = intent.getExtras();
                        String passengerRideId = extras.getString(PASSENGER_RIDE_KEY);

                        loc.setLatitude(mMyLocation.getValue().getLatitude());
                        loc.setLongitude(mMyLocation.getValue().getLongitude());
                        mPassengerRepository.updatePassengerRideCurrentLocation(passengerRideId, loc);
                    }
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

            RemoteViews notificationView = new RemoteViews(this.getPackageName(),R.layout.foreground_notification);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.passenger))
                    .setTicker(getString(R.string.passenger))
                    .setContentText(getString(R.string.passenger))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContent(notificationView)
                    .setOngoing(true).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }

        //Get Driver Position
        //Get Passenger Position

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND)) {
            Toast.makeText(this,"Start Service",Toast.LENGTH_SHORT).show();
        }

        else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND)) {

            Toast.makeText(this,"Stop Service",Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

}
