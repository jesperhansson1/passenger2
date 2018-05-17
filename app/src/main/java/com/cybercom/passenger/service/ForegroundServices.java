package com.cybercom.passenger.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.PassengerRide;
import com.cybercom.passenger.repository.PassengerRepository;
import com.cybercom.passenger.utils.LocationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ForegroundServices extends LifecycleService {


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


    private List<PassengerRide> mPassengerRides;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Uppdatera Drivers position
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_UPDATE_DRIVER_POSITION)) {

            Bundle extras = intent.getExtras();
            String driveId = extras.getString(DRIVE_ID);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        mMyLocation.setValue(location);

                        Timber.d("DriveId %s", driveId);

                        loc.setLatitude(mMyLocation.getValue().getLatitude());
                        loc.setLongitude(mMyLocation.getValue().getLongitude());
                        mPassengerRepository.updateDriveCurrentLocation(driveId, loc);
                        mPassengerRepository.updateDriveCurrentVelocity(driveId, loc.getSpeed());
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

            RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.foreground_notification);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.passenger))
                    .setTicker(getString(R.string.passenger))
                    .setContentText(getString(R.string.passenger))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(notificationView)
                    .setOngoing(true).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);

            mPassengerRepository.getPassengerRides(driveId).observe(this, passengerRide -> {
                if (mPassengerRides == null) {
                    mPassengerRides = new ArrayList<>();
                }

                // If the list already contains a passenger ride with the same id. Update it.
                int passengerRideToReplace = -1;
                for (int i = 0; i < mPassengerRides.size(); i++) {
                    PassengerRide pr = mPassengerRides.get(i);

                    if (passengerRide != null &&
                            pr.getDriveId().equals(passengerRide.getDriveId())) {
                        passengerRideToReplace = i;
                    }
                }

                if (passengerRideToReplace != -1) {
                    mPassengerRides.remove(passengerRideToReplace);
                }

                // Put every accepted passenger ride in a list

                mPassengerRides.add(passengerRide);

            });

            Location loc = new Location("");
            loc.setLatitude(55.614256);
            loc.setLongitude(12.989117);
            mPassengerRepository.getDriverCurrentLocation().observe(this, driverLocation -> {
                if(mPassengerRides != null){
                    for (PassengerRide pr : mPassengerRides) {
                        if (driverLocation != null) {
                            if (driverLocation.distanceTo(LocationHelper
                                    .convertPositionToLocation(pr.getPickUpPosition())) < 10
                                    && !pr.isPickUpConfirmed()) {
                                if (!isAppInBackground(this)) {
                                    showPickUpDialogInUi(pr);
                                } else {
                                    createNotification();
                                }
                            }

                           if(driverLocation.distanceTo(LocationHelper
                                   .convertPositionToLocation(pr.getDropOffPosition())) < 10
                                   && !pr.isDropOffConfirmed()){
                                if (!isAppInBackground(this)) {
                                    showDropOfDialogInUi(pr);
                                } else {
                                    createNotification();
                                }
                            }
                        }
                    }
                }
            });
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
                        mPassengerRepository.updatePassengerRideCurrentLocation(loc);
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
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND)) {

            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void showPickUpDialogInUi(PassengerRide passengerRide) {
        Intent showPickUpDialogInUi = new Intent(MainActivity.INTENT_FILTER_DIALOG_LOCAL_BROADCAST);
        showPickUpDialogInUi.putExtra(MainActivity.DIALOG_TYPE, MainActivity.PASSENGER_PICK_UP);
        showPickUpDialogInUi.putExtra(MainActivity.BROADCAST_PASSENGER_RIDE, passengerRide);
        LocalBroadcastManager.getInstance(this).sendBroadcast(showPickUpDialogInUi);
    }

    private void showDropOfDialogInUi(PassengerRide passengerRide) {
        Intent showDropOffDialogInUi = new Intent(MainActivity.INTENT_FILTER_DIALOG_LOCAL_BROADCAST);
        showDropOffDialogInUi.putExtra(MainActivity.DIALOG_TYPE, MainActivity.PASSENGER_DROP_OFF);
        showDropOffDialogInUi.putExtra(MainActivity.BROADCAST_PASSENGER_RIDE, passengerRide);
        LocalBroadcastManager.getInstance(this).sendBroadcast(showDropOffDialogInUi);
    }

    private void createNotification() {
        Timber.i("Create notification");
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
        super.onBind(intent);
        return null;
    }

    public boolean isAppInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager activityManager
                = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses
                    = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance
                        == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }

        return isInBackground;
    }

}
