package com.cybercom.passenger.flows.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.MainViewModel;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialog;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.i("First log info");
        Fabric.with(this, new Crashlytics());
        addUI();

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (ContextCompat.checkSelfPermission(this.getApplication(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
//            TODO: Handle the case when the user denied permission and chose 'do not ask again'
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//            }
//            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
//            }
        } else {
            viewModel.getLocation();
        }

        viewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                //Timber.d("Value from viewmodel: %Location: ", location);
                if(location == null){
                    System.out.println("get updated --> null");
                } else{
                    System.out.println("get updated location activity: " + location);
                }
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.getLastLocation();
                }
            }
        }
    }

    public void addUI(){
        changeLabelFontStyle(false);
        final Switch switchRide = findViewById(R.id.switch_ride);
        final FloatingActionButton floatRide = findViewById(R.id.button_createRide);
        floatRide.setImageResource(R.drawable.passenger);
        switchRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if(isChecked){
                    floatRide.setImageResource(R.drawable.driver);
                    changeLabelFontStyle(true);
                }
                else
                {
                    floatRide.setImageResource(R.drawable.passenger);
                    changeLabelFontStyle(false);
                }
            }
        });
        floatRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchRide.isChecked()){
                    showCreateDriveDialog();
                }
                else
                {
                    showCreateDriveRequestDialog();
                }
           }
        });
    }

    public void changeLabelFontStyle(boolean driverValue)
    {
        if(driverValue){
            ((TextView)findViewById(R.id.label_driver)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView)findViewById(R.id.label_passenger)).setTypeface(Typeface.DEFAULT);
        }
        else
        {
            ((TextView)findViewById(R.id.label_passenger)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView)findViewById(R.id.label_driver)).setTypeface(Typeface.DEFAULT);
        }
    }

    public void showCreateDriveDialog()
    {
        CreateRideDialog createRideDialog = new CreateRideDialog(MainActivity.this, "Create Drive");
        createRideDialog.getWindow().setGravity(Gravity.BOTTOM);
        createRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createRideDialog.show();
    }

    public void showCreateDriveRequestDialog()
    {
        CreateRideDialog createRideDialog = new CreateRideDialog(MainActivity.this, "Request");
        createRideDialog.getWindow().setGravity(Gravity.BOTTOM);
        createRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createRideDialog.show();
    }
}
