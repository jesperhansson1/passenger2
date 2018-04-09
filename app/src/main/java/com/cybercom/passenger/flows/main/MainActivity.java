package com.cybercom.passenger.flows.main;

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
import com.cybercom.passenger.flows.driverconfirmation.DriverConfirmationDialog;

import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.route.FetchRouteUrl;
import com.cybercom.passenger.utils.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CreateRideDialogFragment.CreateRideDialogFragmentListener, DriverConfirmationDialog.ConfirmationListener, PassengerNotificationDialog.PassengerNotificationListener, OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    MainViewModel mMainViewModel;
    Location mLocation;
    private GoogleMap mGoogleMap;
    Location mMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                mMainViewModel.getIncomingNotifications();
            }
        }

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
        }

        initUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_activitymap_googlemap);
        mapFragment.getMapAsync(this);

        initObservers();

    }

    private void initObservers() {
        mMainViewModel.getIncomingNotifications().observe(this, new Observer<Notification>() {
            @Override
            public void onChanged(@Nullable Notification notification) {
                if (notification == null) return;

                switch (notification.getType()) {
                    case Notification.REQUEST_DRIVE:
                        showDriverConfirmationDialogFragment(notification.getDrive(), notification.getDriveRequest());
                        break;
                    case Notification.ACCEPT_PASSENGER:
                        showPassengerNotificationDialog(notification.getDrive());
                        break;
                    case Notification.REJECT_PASSENGER:
                        // TODO: Attempt to make another match
                }
            }
        });

        mMainViewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                // TODO: Need to handle if there is no data och display info. Need to send this location to spinner
                if(location == null){
                     Timber.d("get updated --> null");
                } else {
                    mLocation = location;
                    //Permission granted to access user location
                    mMyLocation = mLocation;
                }
            }
        });
        if (mLocation == null)
        {
            if(mMainViewModel.getLastSeenLocation() == null)
            {
                //Permission denied to access user location and last seen location is null
                Location defaultLocation = new Location("default");
                defaultLocation.setLatitude(55.611473);
                defaultLocation.setLongitude(12.994266);
                mMyLocation = defaultLocation;
            }
            else
            {
                //Permission denied to access user location and last seen location is not null
                mMyLocation = mMainViewModel.getLastSeenLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMainViewModel.startLocationUpdates();
    }

    public void initUI(){
        changeLabelFontStyle(false);
        final Switch switchRide = findViewById(R.id.switch_ride);
        final FloatingActionButton floatRide = findViewById(R.id.button_createRide);
        floatRide.setImageResource(R.drawable.passenger);
        switchRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if (isChecked) {
                    floatRide.setImageResource(R.drawable.driver);
                    changeLabelFontStyle(true);
                } else {
                    floatRide.setImageResource(R.drawable.passenger);
                    changeLabelFontStyle(false);
                }
            }
        });
        floatRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchRide.isChecked()) {
                    showCreateDriveDialog(CreateRideDialogFragment.TYPE_RIDE, LocationHelper.convertLocationToDisplayString(mLocation));
                } else {
                    showCreateDriveDialog(CreateRideDialogFragment.TYPE_REQUEST, LocationHelper.convertLocationToDisplayString(mLocation));
                }
            }
        });
    }

    public void changeLabelFontStyle(boolean driverValue) {
        if (driverValue) {
            ((TextView) findViewById(R.id.label_driver)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView) findViewById(R.id.label_passenger)).setTypeface(Typeface.DEFAULT);
        } else {
            ((TextView) findViewById(R.id.label_passenger)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView) findViewById(R.id.label_driver)).setTypeface(Typeface.DEFAULT);
        }
    }

    public void showCreateDriveDialog(int type, String location)
    {
        CreateRideDialogFragment dialogFragment = CreateRideDialogFragment.newInstance(type, location);
        dialogFragment.show(getFragmentManager(), CreateRideDialogFragment.TAG);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMinZoomPreference(4.0f);
        mGoogleMap.setMaxZoomPreference(14.0f);
        //To show +/- zoom options
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        updateMyLocation(mMyLocation);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void updateMyLocation(Location myLocation)
    {
        if(mGoogleMap!=null) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("You are Here");
            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 8.0f));
        }
    }

    public void updateMap(LatLng startLocation, LatLng endLocation){

        mGoogleMap.addMarker(new MarkerOptions().position(startLocation).title("Start"));
        mGoogleMap.addMarker(new MarkerOptions().position(endLocation).title("End"));
        LatLngBounds latLngBounds = new LatLngBounds(
                startLocation, endLocation);
        // Constrain the camera target to the Adelaide bounds.
        mGoogleMap.setLatLngBoundsForCameraTarget(latLngBounds);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomIn());
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(5), 2000, null);
        FetchRouteUrl fetchRouteUrl = new FetchRouteUrl(mGoogleMap, startLocation, endLocation);
    }

    @Override
    public void onCreateRide(int type, Position startLocation, Position endLocation) {
        Timber.i("on create ride");
         switch (type) {
            case CreateRideDialogFragment.TYPE_RIDE:
                mMainViewModel.createDrive(startLocation, endLocation);
                updateMap(new LatLng(startLocation.getLatitude(),startLocation.getLongitude()),
                        new LatLng(endLocation.getLatitude(),endLocation.getLongitude()));
                break;
            case CreateRideDialogFragment.TYPE_REQUEST:
                final DriveRequest driveRequest = mMainViewModel.createDriveRequest(startLocation, endLocation);

                mMainViewModel.findBestDriveMatch(startLocation, endLocation).observe(this, new Observer<Drive>() {
                    @Override
                    public void onChanged(@Nullable Drive drive) {
                        Timber.d("DriveRequest: %s is matched to Ride: %s", driveRequest, drive);
                        if (drive != null) {
                            mMainViewModel.addRequestDriveNotification(driveRequest, drive);
                        }
                    }
                });
                break;
        }

    }

    private void showDriverConfirmationDialogFragment(Drive drive, DriveRequest driveRequest) {
        DriverConfirmationDialog dialogFragment = DriverConfirmationDialog.getInstance(drive, driveRequest);
        dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
    }

    private void showPassengerNotificationDialog(Drive drive) {
        PassengerNotificationDialog dialogFragment = PassengerNotificationDialog.getInstance();
        dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
    }

    @Override
    public void onDriverConfirmation(Boolean isAccepted, Drive drive, DriveRequest driveRequest) {
        if (isAccepted) {
            mMainViewModel.sendAcceptPassengerNotification(drive, driveRequest);
        } else {
            mMainViewModel.sendRejectPassengerNotificaiton();
        }
    }

    @Override
    public void onCancelPressed(Boolean isCancelPressed) {
        Timber.i("Canceled pressed! ");
    }
}
