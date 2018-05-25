package com.cybercom.passenger.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.interfaces.OnVelocityCheckedListener;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GeofenceTransitionsIntentService extends IntentService {

    private PassengerRepository mPassengerRepository = PassengerRepository.getInstance();
    private static final Float DRIVER_VELOCITY_THRESHOLD = 3f;
    public static final int SECONDS_DRIVERS_VELOCITY_NEEDS_TO_BE_UNDER_THRESHOLD = 10;
    private boolean mIsVelocityChecking = false;
    private int mSecondsCounter = 0;
    private Handler mVelocityPollingHandler;
    private Runnable mVelocityPollingRunnable;

    private Handler mVelocityCheckHandler;
    private Runnable mVelocityCheckRunnable;
    private Float mSumVelocity;
    private List<Float> mVelocityAverage = new ArrayList<>();
    private static final long COUNT_INTERVAL = 1000;
    private Float mDriversVelocity;
    private LiveData<Float> mGetVelocity;
    private Observer<Float> mVelocityObserver;
    private GeofencingEvent mGeofencingEvent;

    public GeofenceTransitionsIntentService() {
        super("GeofenceThread");
    }

    public GeofenceTransitionsIntentService(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mGeofencingEvent = GeofencingEvent.fromIntent(intent);
        Timber.i("Inside Geofence");
        if (mGeofencingEvent.hasError()) {
            Timber.e("Couldn't get the event: %s", mGeofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = mGeofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Timber.i("Inside Transition Enter");
            mVelocityPollingRunnable = () -> {
                mDriversVelocity = mPassengerRepository.getDriverCurrentVelocity();
                Timber.i("Polling velocity %s", mDriversVelocity);

                if (mDriversVelocity < DRIVER_VELOCITY_THRESHOLD && !mIsVelocityChecking) {
                    mIsVelocityChecking = true;
                    checkIfVelocityIsUnderThresholdForAPeriodOfTime(
                            this::calculateIfAverageVelocityIsUnderThreshold);
                }

                mVelocityPollingHandler.postDelayed(mVelocityPollingRunnable, COUNT_INTERVAL);
            };

            mVelocityPollingHandler = new Handler(Looper.getMainLooper());
            mVelocityPollingHandler.postDelayed(mVelocityPollingRunnable, COUNT_INTERVAL);

        } else {
            Timber.e("Couldn't find a transition matching GEOFENCE_TRANSITION_ENTER");
        }
    }

    private void calculateIfAverageVelocityIsUnderThreshold() {
        for (Float velocity : mVelocityAverage) {
            mSumVelocity = +velocity;
        }

        if (mSumVelocity / mVelocityAverage.size() < DRIVER_VELOCITY_THRESHOLD) {
            List<Geofence> triggeringGeofences
                    = mGeofencingEvent.getTriggeringGeofences();
            showDialogInUi(triggeringGeofences.get(0).getRequestId());
            mVelocityPollingHandler.removeCallbacks(mVelocityPollingRunnable);
            Timber.i("Id of the triggered geofenceEvents: %s",
                    triggeringGeofences.get(0).getRequestId());
        }

        mIsVelocityChecking = false;
        mSumVelocity = 0f;
        mVelocityAverage.clear();
    }

    private void showDialogInUi(String requestId) {
        Intent showDialogInUiIntent = new Intent(MainActivity.GEOFENCE_EVENTS_INTENT_FILTER);
        showDialogInUiIntent.putExtra(MainActivity.GEOFENCE_EVENTS_REQUEST_ID, requestId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(showDialogInUiIntent);
    }

    private void checkIfVelocityIsUnderThresholdForAPeriodOfTime(
            OnVelocityCheckedListener onVelocityCheckedListener) {

        mIsVelocityChecking = true;

        mVelocityCheckHandler = new Handler();
        mVelocityCheckRunnable = () -> {
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
