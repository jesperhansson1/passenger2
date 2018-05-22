package com.cybercom.passenger.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.cybercom.passenger.flows.main.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import timber.log.Timber;

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super("GeofenceThread");
    }

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Timber.e("Couldn't get the event: %s", geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            showDialogInUi(triggeringGeofences.get(0).getRequestId());
            Timber.i("Id of the triggered geofenceEvents: %s",
                    triggeringGeofences.get(0).getRequestId());

        } else {
            Timber.e("Couldn't find a transition matching GEOFENCE_TRANSITION_ENTER");
        }
    }

    private void showDialogInUi(String requestId) {
        Intent showDialogInUiIntent = new Intent(MainActivity.GEOFENCE_EVENTS_INTENT_FILTER);
        showDialogInUiIntent.putExtra(MainActivity.GEOFENCE_EVENTS_REQUEST_ID, requestId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(showDialogInUiIntent);
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
