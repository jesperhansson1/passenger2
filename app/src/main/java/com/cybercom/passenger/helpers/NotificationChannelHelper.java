package com.cybercom.passenger.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

import android.os.Build;

import com.cybercom.passenger.R;

public class NotificationChannelHelper extends ContextWrapper {

    public static final String PRIMARY_CHANNEL_ID = "com.cybercom.passenger.NOTIFICATION";
    private NotificationManager manager;

    /**
     * Register notification channel
     *
     * @param context Application context
     */

    public NotificationChannelHelper(Context context) {
        super(context);

        NotificationChannel passengerPrimaryChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            passengerPrimaryChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    getString(R.string.notification_primary_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);

            getManager().createNotificationChannel(passengerPrimaryChannel);
        }
    }

    /**
     * Get the notification manager.
     *
     * @return Notification manager
     */
    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}
