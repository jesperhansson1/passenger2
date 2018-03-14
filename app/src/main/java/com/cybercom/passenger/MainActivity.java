package com.cybercom.passenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import timber.log.Timber;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Timber.i("First log info");
        Fabric.with(this, new Crashlytics());

    }
}
