package com.cybercom.passenger.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.interfaces.OnVelocityCheckedListener;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ForegroundServices extends LifecycleService {

    public static final int TIME_BETWEEN_ETA_LOOKUPS_DELAY_MILLIS = 30 * 1000;
    private static final long FIRST_TIME_ETA_LOOKUP_DELAY_MILLIS = 2000;
    private static final long FIRST_ETA_NOTIFICATION_TIME_SECONDS = 60 * 10;
    private static final long SECOND_ETA_NOTIFICATION_TIME_SECONDS = 60 * 7;
    private static final long THIRD_ETA_NOTIFICATION_TIME_SECONDS = 60 * 3;
    public static final int DISTANCE_FOR_DETECTING_ARRIVAL_OF_DRIVER = 80;
    private static final long TIME_BETWEEN_ARRIVAL_DETECTION = 1000;
    private static final Float DRIVER_VELOCITY_THRESHOLD = 3f;
    private static final long COUNT_INTERVAL = 1000;
    public static final int SECONDS_DRIVERS_VELOCITY_NEEDS_TO_BE_UNDER_THRESHOLD = 10;
    private long FIRST_TIME_DETECT_DELAY_MILLIS = 2000;
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
    private long mLastETA;
    private Float mDriversVelocity;
    private boolean mDropOffConfirmed;
    private float[] distanceBetweenDriverAndPickUpLocation = new float[1];
    private Position mDropOffLocation;
    private float[] distanceBetweenDriverAndDropOffLocation = new float[1];
    private boolean mIsVelocityChecking = false;
    private Float mSumVelocity;
    private List<Float> mVelocityAverage = new ArrayList<>();
    private boolean mIsDriverAtPickUpLocation = false;
    private boolean mIsDriverAtDropOffLocation = false;
    private int mSecondsCounter = 0;
    private Handler mVelocityCheckHandler;
    private Runnable mVelocityCheckRunnable;
    private boolean mCancelAllFlag;


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
        //Update Drivers position
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_DRIVER_CLIENT)) {

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
                        Timber.d("DriveId %s", driveId);

                        mCurrentLocation = location;
                    }

                    mMyLocationMutableLiveData.setValue(mCurrentLocation);

                    float distanceDelta = mCurrentLocation.distanceTo(prevLocation);
                    float timeDelta = Math.abs(mCurrentLocation.getTime() - prevLocation.getTime()) / 1000f;
                    float speed = 1;

                    if (timeDelta != 0.0f) {
                        speed = distanceDelta / timeDelta;
                    }

                    Timber.d("currentSpeed: %f", speed);
                    Timber.d("currentBearing: %f", mCurrentLocation.getBearing());

                    mPassengerRepository.updateDriveCurrentLocation(driveId, mCurrentLocation);
                    mPassengerRepository.updateDriveCurrentVelocity(driveId, speed);
                }
            };

            createLocationRequest();
            startLocationUpdates();

            startForegroundService();
        }

        //Uppdatera Passengers position
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_PASSENGER_CLIENT)) {

            PassengerRepository.getInstance().getDriverPosition(driveId).observe(this,
                    position -> mDriversPosition = position);
            PassengerRepository.getInstance().getDriverVelocity(driveId).observe(this,
                    velocity -> mDriversVelocity = velocity);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        mCurrentLocation = location;
                    }
                    mMyLocationMutableLiveData.setValue(mCurrentLocation);
                    mPassengerRepository.updatePassengerRideCurrentLocation(mCurrentLocation);
                }
            };

            mPassengerRepository.getPassengerRideById(passengerRideId).observe(this,
                passengerRide -> {
                    if (passengerRide == null) {
                        mCancelAllFlag = true;
                        return;
                    }
                    mPickUpLocation = passengerRide.getPickUpPosition();
                    mDropOffLocation = passengerRide.getDropOffPosition();
                    boolean isPickUpConfirmed = passengerRide.isPickUpConfirmed();

                    // If passenger is picked up terminate this service
                    if (!mPickUpConfirmed && isPickUpConfirmed) {
                        stopLocationUpdates();
                        stopForeground(true);
                    }
                    mPickUpConfirmed = passengerRide.isPickUpConfirmed();
                    mDropOffConfirmed = passengerRide.isDropOffConfirmed();

                    new Handler().postDelayed(this::requestETAInBackgroundAndUpdateToRepository,
                            FIRST_TIME_ETA_LOOKUP_DELAY_MILLIS);
                    new Handler().postDelayed(this::detectDriverArrival,
                            FIRST_TIME_DETECT_DELAY_MILLIS);

                    startScheduledETACalculation();
                });

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
            createLocationRequest();
            startLocationUpdates();

            startForegroundService();
        }

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND)) {
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND)) {

            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        Notification notification = buildForegroundServiceNotification();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
    }

    private Notification buildForegroundServiceNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.foreground_notification);

        return new NotificationCompat.Builder(this,
                NotificationHelper.TRACKING_CHANNEL)
                .setContentTitle(getString(R.string.passenger))
                .setTicker(getString(R.string.passenger))
                .setContentText(getString(R.string.passenger))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(notificationView)
                .setOngoing(true).build();
    }

    private void detectDriverArrival() {
        new Handler().postDelayed(() -> {

            if (mCancelAllFlag) {
                return;
            }

            if (mDriversPosition == null || mPickUpLocation == null || mDropOffLocation == null
                    || mDriversVelocity == null) {
                return;
            }

            Timber.i("detectArrival (mDriversPosition): %s", mDriversPosition);
            Timber.i("detectArrival (mPickUpLocation): %s", mPickUpLocation);
            Timber.i("detectArrival (mDriversVelocity): %s", mDriversVelocity);

            if (!mPickUpConfirmed) {
                detectArrivalToPickUpLocation();
            }

            if (!mDropOffConfirmed) {
                detectArrivalToDropOffLocation();
//                return;
            }

            detectDriverArrival();
        }, TIME_BETWEEN_ARRIVAL_DETECTION);
    }

    private void detectArrivalToPickUpLocation() {
        if (mIsVelocityChecking) {
            return;
        }
        Location.distanceBetween(mPickUpLocation.getLatitude(),
                mPickUpLocation.getLongitude(),
                mDriversPosition.getLatitude(), mDriversPosition.getLongitude(),
                distanceBetweenDriverAndPickUpLocation);

        if (distanceBetweenDriverAndPickUpLocation[0] < DISTANCE_FOR_DETECTING_ARRIVAL_OF_DRIVER) {

            checkIfVelocityIsUnderThresholdForAPeriodOfTime(() -> {

                for (Float velocity : mVelocityAverage) {
                    mSumVelocity = +velocity;
                }

                if (mSumVelocity / mVelocityAverage.size() < DRIVER_VELOCITY_THRESHOLD) {
                    mPassengerRepository.updateETA(PassengerRepository.UNDEFINED_ETA);
                    if(isAppInBackground(this)){
                        showNotification(MainActivity.TYPE_PICK_UP);
                    } else {
                        showDialogInUi(MainActivity.TYPE_PICK_UP);
                    }

                    mPickUpConfirmed = true;
                }

                mIsVelocityChecking = false;
                mSumVelocity = 0f;
                mVelocityAverage.clear();

            });
        }
    }

    private void detectArrivalToDropOffLocation() {
        if (mIsVelocityChecking) {
            return;
        }

        Location.distanceBetween(mDropOffLocation.getLatitude(),
                mDropOffLocation.getLongitude(),
                mDriversPosition.getLatitude(), mDriversPosition.getLongitude(),
                distanceBetweenDriverAndDropOffLocation);

        if (distanceBetweenDriverAndDropOffLocation[0] < DISTANCE_FOR_DETECTING_ARRIVAL_OF_DRIVER) {

            checkIfVelocityIsUnderThresholdForAPeriodOfTime(() -> {
                for (Float velocity : mVelocityAverage) {
                    mSumVelocity = +velocity;
                }

                if (mSumVelocity / mVelocityAverage.size() < DRIVER_VELOCITY_THRESHOLD) {
                    if(isAppInBackground(this)){
                        showNotification(MainActivity.TYPE_DROP_OFF);
                    }else{
                        showDialogInUi(MainActivity.TYPE_DROP_OFF);
                    }
                    mDropOffConfirmed = true;
                }

                mIsVelocityChecking = false;
                mSumVelocity = 0f;
                mVelocityAverage.clear();

            });
        }
    }

    private void showNotification(int type) {
        // TODO implement notification when app is in background
    }

    private void showDialogInUi(int type) {
        Intent intent = new Intent(MainActivity.FOREGROUND_SERVICE_INTENT_FILTER);
        intent.putExtra(MainActivity.DIALOG_TO_SHOW, type);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void checkIfVelocityIsUnderThresholdForAPeriodOfTime(
            OnVelocityCheckedListener onVelocityCheckedListener) {

        mIsVelocityChecking = true;

        mVelocityCheckHandler = new Handler();
        mVelocityCheckRunnable = () -> {
            if (mCancelAllFlag) {
                return;
            }
            mSecondsCounter++;
            mVelocityAverage.add(mDriversVelocity);

            if (mSecondsCounter == SECONDS_DRIVERS_VELOCITY_NEEDS_TO_BE_UNDER_THRESHOLD) {
                onVelocityCheckedListener.onVelocityChecked();
                mVelocityCheckHandler.removeCallbacks(mVelocityCheckRunnable);
            }

            mVelocityCheckHandler.postDelayed(mVelocityCheckRunnable, COUNT_INTERVAL);

        };

        mVelocityCheckHandler.postDelayed(mVelocityCheckRunnable, COUNT_INTERVAL);
    }

    private void startScheduledETACalculation() {
        new Handler().postDelayed(() -> {

            requestETAInBackgroundAndUpdateToRepository();

            if (mDropOffConfirmed) {
                return;
            }
            // Re-run this until passenger is picked up or is dismissed or cancels
            startScheduledETACalculation();
        }, TIME_BETWEEN_ETA_LOOKUPS_DELAY_MILLIS);
    }

    private void requestETAInBackgroundAndUpdateToRepository() {
        if (mDriversPosition == null || mPickUpLocation == null || mDropOffLocation == null) {
            return;
        }

        Position endPosition = mPickUpConfirmed ? mDropOffLocation : mPickUpLocation;

        // TODO, this information is in the route/leg..
        DistantMatrixAPIHelper.getInstance().mMatrixAPIService.getDistantMatrix(
                LocationHelper.getStringFromLatLng(
                        mDriversPosition.getLatitude(), mDriversPosition.getLongitude()),
                LocationHelper.getStringFromLatLng(
                        endPosition.getLatitude(), endPosition.getLongitude()),
                getResources().getString(R.string.google_api_key)).enqueue(new Callback<DistanceMatrixResponse>() {
            @Override
            public void onResponse(Call<DistanceMatrixResponse> call, Response<DistanceMatrixResponse> response) {
                if (response.isSuccessful()) {
                    long duration = DistantMatrixAPIHelper.getDurationFromResponse(response, 0, 0);
//                    long dis = DistantMatrixAPIHelper.getDistanceFromResponse(response, 0, 0);
                    Timber.d("eta: %s", duration);
                    if (!mPickUpConfirmed) {
                        sendETANotificationIfItIsTime(duration, mLastETA);
                    }
                    if (duration != mLastETA) {
                        mPassengerRepository.updateETA(duration);
                        mLastETA = duration;
                    }
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

    public void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
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


    /**
     * @param time The estimated time of arrival (in minutes) to be displayed
     */
    private void showETANotification(int time) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this,
                NotificationHelper.TRACKING_CHANNEL)
                .setSmallIcon(R.drawable.driver)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(getString(R.string.eta_notification_title, time))
                .setContentText(getString(R.string.eta_notification_text, time))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }

    private void sendETANotificationIfItIsTime(long eTA, long lastETA) {
        if ((eTA < FIRST_ETA_NOTIFICATION_TIME_SECONDS && lastETA > FIRST_ETA_NOTIFICATION_TIME_SECONDS) |
                (eTA < SECOND_ETA_NOTIFICATION_TIME_SECONDS && lastETA > SECOND_ETA_NOTIFICATION_TIME_SECONDS) |
                (eTA < THIRD_ETA_NOTIFICATION_TIME_SECONDS && lastETA > THIRD_ETA_NOTIFICATION_TIME_SECONDS)) {

            showETANotification(Math.round(eTA / 60));
        }
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
