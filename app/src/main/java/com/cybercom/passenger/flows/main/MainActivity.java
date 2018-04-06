package com.cybercom.passenger.flows.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.cybercom.passenger.flows.login.Login;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CreateRideDialogFragment.CreateRideDialogFragmentListener, DriverConfirmationDialog.ConfirmationListener, PassengerNotificationDialog.PassengerNotificationListener, OnMapReadyCallback {

    FirebaseUser mUser;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    MainViewModel mMainViewModel;
    Location mLocation;
    private GoogleMap mGoogleMap;
    Menu mLoginMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        Timber.i("First log info");
        Fabric.with(this, new Crashlytics());

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                mMainViewModel.getIncomingNotifications();
            }
        }
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            getSupportActionBar().setTitle(mUser.getEmail());
        } else {
            getSupportActionBar().setTitle(R.string.mainactivity_title);
        }

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivity_login, menu);
        mLoginMenu = menu;

        if (mUser != null) {
            mLoginMenu.findItem(R.id.menu_action_login).setVisible(false);
        } else {
            mLoginMenu.findItem(R.id.menu_action_login).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (menuId == R.id.menu_action_login) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
            mMainViewModel.startLocationUpdates();
    }

    public void getViewModel(){
        mMainViewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                // TODO: Need to handle if there is no data och display info. Need to send this location to spinner
                if(location == null){
                    Timber.d("get updated --> null");
                } else {
                    mLocation = location;
                }
            }
        });
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
        mGoogleMap.setMinZoomPreference(10.0f);
        mGoogleMap.setMaxZoomPreference(20.0f);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        LatLng opera = new LatLng(-33.9320447,151.1597271);
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mGoogleMap.addMarker(new MarkerOptions().position(opera).title("Marker in Opera"));
        LatLngBounds ADELAIDE = new LatLngBounds(
                sydney, opera);
        // Constrain the camera target to the Adelaide bounds.
        mGoogleMap.setLatLngBoundsForCameraTarget(ADELAIDE);
       // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        FetchRouteUrl fetchRouteUrl = new FetchRouteUrl(mGoogleMap, sydney, opera);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onCreateRide(int type, Position startLocation, Position endLocation) {
        Timber.i("on create ride");

        switch (type) {
            case CreateRideDialogFragment.TYPE_RIDE:
                mMainViewModel.createDrive(startLocation, endLocation);
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