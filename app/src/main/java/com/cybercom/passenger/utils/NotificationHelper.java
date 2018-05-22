package com.cybercom.passenger.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

import com.cybercom.passenger.R;

public class NotificationHelper extends ContextWrapper {

    public static final String TRACKING_CHANNEL = "tracking";
    private NotificationManager mNotificationManager;

    public NotificationHelper(Context context) {
        super(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel trackingChannel = new NotificationChannel(
                    TRACKING_CHANNEL,
                    getString(R.string.tracking_notification_channel),
                    NotificationManager.IMPORTANCE_LOW);

            getNotificationManager().createNotificationChannel(trackingChannel);
        }
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
}
