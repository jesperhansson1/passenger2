package com.cybercom.passenger.flows.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this.getApplication(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permission was not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                System.out.println("We are inside the 'shouldShowRequestPermissionRationale' section");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                System.out.println("Request permission");
            }
        } else {
            viewModel.getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permission was granted!!");
                    viewModel.getLastLocation();
                } else {
                    System.out.println("Permission was denied! 😞");
                }
                return;
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
