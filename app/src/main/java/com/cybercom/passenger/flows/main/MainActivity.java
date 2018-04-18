package com.cybercom.passenger.flows.main;

import android.Manifest;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialogFragment;
import com.cybercom.passenger.flows.createridefragment.CreateDriveFragment;
import com.cybercom.passenger.flows.driverconfirmation.AcceptRejectPassengerDialog;
import com.cybercom.passenger.flows.login.LoginActivity;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.route.FetchRouteUrl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CreateRideDialogFragment.CreateRideDialogFragmentListener, AcceptRejectPassengerDialog.ConfirmationListener, PassengerNotificationDialog.PassengerNotificationListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraMoveStartedListener {

    FirebaseUser mUser;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    MainViewModel mMainViewModel;
    Location mLocation;
    Menu mLoginMenu;
    FloatingActionButton mFloatRide;

    private FragmentManager mFragmentManager;
    CreateDriveFragment mCreateDriveFragment;
    private boolean isCreateDriveFragmentVisible = false;


    private GoogleMap mGoogleMap;
    private Marker mStartLocationMarker;
    private Marker mEndLocationMarker;
    ;
    private boolean isStartLocationMarkerAdded = false;
    private int mMarkerCount = 0;
    private boolean isEndLocationMarkerAdded = false;

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
        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                mMainViewModel.setIncomingNotification(getIntent().getExtras());
            }
        }
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            mMainViewModel.refreshToken(FirebaseInstanceId.getInstance().getToken());
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mUser.getEmail());
            }
            mMainViewModel.getUser().observe(this, new Observer<User>() {
                @Override
                public void onChanged(@Nullable User user) {
                    Timber.i("User: %s logged in", user);
                }
            });
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.mainactivity_title);
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
            public void onChanged(@Nullable final Notification notification) {
                if (notification == null) return;
                Timber.d("Notification to be displayed: %s", notification.toString());

                switch (notification.getType()) {
                    case Notification.REQUEST_DRIVE:
                        showDriverConfirmationDialogFragment(notification);
                        mMainViewModel.dismissNotification();
                        break;
                    case Notification.ACCEPT_PASSENGER:
                        showPassengerNotificationDialog(notification);
                        mMainViewModel.dismissNotification();
                        break;
                    case Notification.REJECT_PASSENGER:
                        matchDriveRequest(notification.getDriveRequest());
                        mMainViewModel.dismissNotification();
                        break;
                }
            }
        });


        // TODO Only start this when drive is started
       /* mMainViewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                // TODO: Need to handle if there is no data och display info. Need to send this location to spinner
                if (location == null) {
                    Timber.i("get updated --> null");
                } else {
                    mLocation = location;

                }
            }
        });*/

        /*
        if (mLocation == null) {
            // Permission not granted to access user location
            Timber.i("get updated --> minc");
            setDefaultLocationToMinc();
        }*/
    }

    private void addStartLocationMarker() {

        if (mMainViewModel.getStartMarkerLocation().getValue() != null) {
            LatLng startLatLng = new LatLng(mMainViewModel.getStartMarkerLocation().getValue().getLatitude(),
                    mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

            mStartLocationMarker =
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(startLatLng)
                            .title(getString(R.string.marker_title_start_location))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_start_location))
                            .anchor(0.5f, 0.5f)
                            .draggable(true));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 16.0f));

            mMarkerCount++;
            isStartLocationMarkerAdded = true;
        }

        mMainViewModel.getStartMarkerLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                if (location != null) {
                    mStartLocationMarker
                            .setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    private void matchDriveRequest(final DriveRequest driveRequest) {
        final LifecycleOwner lifecycleOwner = this;

        final LiveData<Drive> findMatch = mMainViewModel.findBestDriveMatch(driveRequest);

        final LiveData<Boolean> timerObserver = mMainViewModel.setFindMatchTimer();
        final Observer<Drive> matchObserver = new Observer<Drive>() {
            @Override
            public void onChanged(@Nullable Drive drive) {
                if (drive != null) {
                    Timber.i("matched drive %s", drive);
                    mMainViewModel.addRequestDriveNotification(driveRequest, drive);
                    findMatch.removeObservers(lifecycleOwner);
                    timerObserver.removeObservers(lifecycleOwner);
                } else {
                    Timber.i("No drives match for the moment. Keep listening.");
                }
            }
        };

        findMatch.observe(lifecycleOwner, matchObserver);


        timerObserver.observe(lifecycleOwner, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                Toast.makeText(MainActivity.this, R.string.main_activity_match_not_found_message,
                        Toast.LENGTH_LONG).show();
                findMatch.removeObserver(matchObserver);
                timerObserver.removeObservers(lifecycleOwner);
            }
        });
    }

    private void addEndLocationMarker() {
        LatLng startLatLng = new LatLng(55.613, 12.9891);
        mEndLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title(getString(R.string.marker_title_end_location))
                .anchor(0.5f, 0.5f)
                .draggable(true));
        isEndLocationMarkerAdded = true;
        Location markerLocation = new Location(getString(R.string.location_provider));
        markerLocation.setLatitude(55.613);
        markerLocation.setLongitude(12.9891);
        mMainViewModel.setEndMarkerLocation(markerLocation);

        mMarkerCount++;

        mMainViewModel.getEndMarkerLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                if (location != null) {
                    mEndLocationMarker
                            .setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    public void setDefaultLocationToMinc() {
        mLocation = new Location("Minc");
        mLocation.setLatitude(55.611473);
        mLocation.setLongitude(12.994266);
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
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // mMainViewModel.startLocationUpdates();
    }

    public void initUI() {
        changeLabelFontStyle(false);
        final Switch switchRide = findViewById(R.id.switch_ride);
        mFloatRide = findViewById(R.id.button_createRide);
        mFloatRide.setImageResource(R.drawable.passenger);
        switchRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if (isChecked) {
                    mFloatRide.setImageResource(R.drawable.driver);
                    changeLabelFontStyle(true);
                } else {
                    mFloatRide.setImageResource(R.drawable.passenger);
                    changeLabelFontStyle(false);
                }
            }
        });

        mCreateDriveFragment = CreateDriveFragment.newInstance();
        mFloatRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isCreateDriveFragmentVisible) {
                    loadFragment(mCreateDriveFragment);
                    if (!isStartLocationMarkerAdded) {
                        mMainViewModel.getLastKnownLocation(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                mMainViewModel.setStartMarkerLocation(location);
                                addStartLocationMarker();
                            }
                        });

                        if (!isEndLocationMarkerAdded) {
                            addEndLocationMarker();
                        }

                    }

                    mFloatRide.setVisibility(View.INVISIBLE);
                    isCreateDriveFragmentVisible = true;
                }

                /*if (switchRide.isChecked()) {
                    showCreateDriveDialog(CreateRideDialogFragment.TYPE_RIDE, LocationHelper.convertLocationToDisplayString(mLocation));
                } else {
                    showCreateDriveDialog(CreateRideDialogFragment.TYPE_REQUEST, LocationHelper.convertLocationToDisplayString(mLocation));
                }*/
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

    public void showCreateDriveDialog(int type, String location) {
        CreateRideDialogFragment dialogFragment = CreateRideDialogFragment.newInstance(type, location);
        dialogFragment.show(getFragmentManager(), CreateRideDialogFragment.TAG);
    }

    private Boolean isFragmentAdded = false;

    public void loadFragment(Fragment fragment) {
        if (isFragmentAdded) {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_enter_animation, R.anim.dialog_exit_animation)
                    .show(fragment).commit();
        } else {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_enter_animation, R.anim.dialog_exit_animation)
                    .replace(R.id.main_activity_dialog_container, fragment).commit();
            isFragmentAdded = true;
        }

    }

    public void hideFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .hide(fragment).commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnCameraMoveStartedListener(this);

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideFragmentAnimation(mCreateDriveFragment);
            }
        });

        mGoogleMap.setMinZoomPreference(4.0f);
        //To show +/- zoom options
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.setOnMarkerDragListener(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);

        if (!mMainViewModel.isInitialZoomDone()) {
            mMainViewModel.getLastKnownLocation(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LatLng initialZoom = new LatLng(
                            location.getLatitude(),
                            location.getLongitude());

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initialZoom, 16.0f));
                    mMainViewModel.setInitialZoomDone(true);
                }
            });
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            hideFragmentAnimation(mCreateDriveFragment);
        }
    }

    public void hideFragmentAnimation(final Fragment fragment) {
        if (isCreateDriveFragmentVisible) {
            mFloatRide.setVisibility(View.VISIBLE);

            if (fragment != null) {
                //Your animation
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.dialog_exit_animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        hideFragment(fragment);

                        isCreateDriveFragmentVisible = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                //Start the animation.
                findViewById(R.id.create_drive_dialog).startAnimation(animation);
            }

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void updateLocation(Marker marker, Location location) {
        if (mGoogleMap != null) {
            marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    public void updateMap(LatLng startLocation, LatLng endLocation) {

        mGoogleMap.addMarker(new MarkerOptions().position(startLocation).title(getApplicationContext().getResources().getString(R.string.start_location)));
        mGoogleMap.addMarker(new MarkerOptions().position(endLocation).title(getApplicationContext().getResources().getString(R.string.end_location)));
        LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();
        latlngBuilder.include(startLocation);
        latlngBuilder.include(endLocation);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), 100));
        FetchRouteUrl fetchRouteUrl = new FetchRouteUrl(mGoogleMap, startLocation, endLocation);

    }

    @Override
    public void onCreateRide(int type, final Position startLocation, final Position endLocation) {
        Timber.i("on create ride");

        switch (type) {
            case CreateRideDialogFragment.TYPE_RIDE:
                long time = System.currentTimeMillis();
                int availableSeats = 4;

                mMainViewModel.createDrive(time ,startLocation, endLocation, availableSeats).observe(this, new Observer<Drive>() {
                    @Override
                    public void onChanged(@Nullable Drive drive) {
                        Timber.i("Drive created: %s", drive);
                    }
                });
                break;
            case CreateRideDialogFragment.TYPE_REQUEST:
                final LifecycleOwner lifeCycleOwner = this;
                long t = System.currentTimeMillis();
                final int seats = 2;
                mMainViewModel.createDriveRequest(t, startLocation, endLocation, seats).observe(this, new Observer<DriveRequest>() {
                    @Override
                    public void onChanged(@Nullable DriveRequest driveRequest) {
                        Timber.i("DriveRequest : %s", driveRequest);
                        matchDriveRequest(driveRequest);
                    }
                });
                break;
        }

    }

    private void showDriverConfirmationDialogFragment(Notification notification) {
        AcceptRejectPassengerDialog dialogFragment = AcceptRejectPassengerDialog.getInstance(notification);
        dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
    }

    private void showPassengerNotificationDialog(Notification notification) {
        PassengerNotificationDialog dialogFragment = PassengerNotificationDialog.getInstance(notification);
        dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
    }

    @Override
    public void onDriverConfirmation(Boolean isAccepted, Notification notification) {
        if (isAccepted) {
            mMainViewModel.sendAcceptPassengerNotification(notification.getDrive(), notification.getDriveRequest());
        } else {
            mMainViewModel.sendRejectPassengerNotification(notification.getDrive(), notification.getDriveRequest());
        }
        mMainViewModel.pollNotificationQueue(notification);
    }

    @Override
    public void onCancelDrive(Notification notification) {
        mMainViewModel.pollNotificationQueue(notification);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if (marker.getTitle().equals(getString(R.string.marker_title_start_location))) {
            Location markerLocation = new Location(getString(R.string.location_provider));
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            mMainViewModel.setStartMarkerLocation(markerLocation);
        }

        if (marker.getTitle().equals(getString(R.string.marker_title_end_location))) {
            Location markerLocation = new Location(getString(R.string.location_provider));
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            mMainViewModel.setEndMarkerLocation(markerLocation);
        }

        if (mMarkerCount == 2) {
            if (mMainViewModel.getStartMarkerLocation().getValue() != null
                    && mMainViewModel.getEndMarkerLocation().getValue() != null) {
                LatLng origin = new LatLng(mMainViewModel.getStartMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

                LatLng destination = new LatLng(mMainViewModel.getEndMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getEndMarkerLocation().getValue().getLongitude());

                new FetchRouteUrl(mGoogleMap, origin, destination);
            }
        }
    }


}
