package com.cybercom.passenger.flows.main;

import android.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.MainViewModel;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialogFragment;
import com.cybercom.passenger.helpers.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    MainViewModel viewModel;
    Location mLocation;
    private GoogleMap mGoogleMap;

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

        getViewModel();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_activitymap_googlemap);
        mapFragment.getMapAsync(this);
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

    public void getViewModel(){
        viewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
//                TODO: Need to handle if there is no data och display info. Need to send this location to spinner
                if(location == null){
                    Timber.d("get updated --> null");
                } else{
                    mLocation = location;
                }
            }
        });
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
                if(switchRide.isChecked()) {
                    showCreateDriveDialog(CreateRideDialogFragment.TYPE_RIDE, LocationHelper.convertLocationToDisplayString(mLocation));
                }
                else {
                    showCreateDriveDialog(CreateRideDialogFragment.TYPE_REQUEST, LocationHelper.convertLocationToDisplayString(mLocation));
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

    public void showCreateDriveDialog(int type, String location)
    {
        DialogFragment dialogFragment = CreateRideDialogFragment.newInstance(type, location);
        dialogFragment.show(getFragmentManager(), CreateRideDialogFragment.TAG);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        System.out.println("inside");
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
