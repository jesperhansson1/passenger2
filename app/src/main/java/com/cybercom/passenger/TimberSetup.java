package com.cybercom.passenger;

import android.app.Application;

import timber.log.Timber;

public class TimberSetup extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
