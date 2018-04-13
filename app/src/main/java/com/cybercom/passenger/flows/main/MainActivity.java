package com.cybercom.passenger.flows.main;

import android.arch.lifecycle.LifecycleOwner;
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

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.CreateDriveFragment;
import com.cybercom.passenger.MainViewModel;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialogFragment;
import com.cybercom.passenger.flows.driverconfirmation.AcceptRejectPassengerDialog;
import com.cybercom.passenger.flows.login.LoginActivity;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.cybercom.passenger.route.FetchRouteUrl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CreateRideDialogFragment.CreateRideDialogFragmentListener, AcceptRejectPassengerDialog.ConfirmationListener, PassengerNotificationDialog.PassengerNotificationListener, OnMapReadyCallback {

    FirebaseUser mUser;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    MainViewModel mMainViewModel;
    Location mLocation;
    private GoogleMap mGoogleMap;
    Menu mLoginMenu;
    FloatingActionButton mFloatRide;

    private FragmentManager mFragmentManager;
    CreateDriveFragment mCreateDriveFragment;
    private boolean isCreateDriveFragmentVisible = false;

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
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCreateDriveFragmentVisible) {
                    mFloatRide.setVisibility(View.VISIBLE);

                    if (mCreateDriveFragment != null) {
                        //Your animation
                        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.dialog_exit_animation);

                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                closeFragment(mCreateDriveFragment);

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
        });

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
                        mMainViewModel.removeNotification();
                        break;
                    case Notification.ACCEPT_PASSENGER:
                        showPassengerNotificationDialog(notification);
                        mMainViewModel.removeNotification();
                        break;
                    case Notification.REJECT_PASSENGER:
                        // TODO: Currently the matching does not handle configuration changes...
                        // Also matching should timeout
//                        mMainViewModel.findBestDriveMatch(notification.getDriveRequest().getStartLocation(), notification.getDriveRequest().getEndLocation()).observe(lifecycleOwner, new Observer<Drive>() {
//                            @Override
//                            public void onChanged(@Nullable Drive drive) {
//                                if (drive != null) {
//                                    mMainViewModel.addReque                                                                                                                   stDriveNotification(notification.getDriveRequest(), drive);
//                                }
//                            }
//                        });

                        mMainViewModel.removeNotification();
                        break;
                }
            }
        });

        mMainViewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                // TODO: Need to handle if there is no data och display info. Need to send this location to spinner
                if (location == null) {
                    Timber.i("get updated --> null");
                } else {
                    mLocation = location;
                }
            }
        });
       // if (mLocation == null) {
            //Permission not granted to access user location
           // Timber.i("get updated --> minc");
          //  setDefaultLocationToMinc();
        //}
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
        mMainViewModel.startLocationUpdates();
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

             if(!isCreateDriveFragmentVisible){
                    loadFragment(mCreateDriveFragment);
                    isCreateDriveFragmentVisible = true;
                 mFloatRide.setVisibility(View.INVISIBLE);
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

    public void loadFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.dialog_enter_animation, R.anim.dialog_exit_animation)
                .replace(R.id.main_activity_dialog_container, fragment).commit();
    }

    public void closeFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .remove(fragment).commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMinZoomPreference(4.0f);
        mGoogleMap.setMaxZoomPreference(14.0f);
        //To show +/- zoom options
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        updateMyLocation(mLocation);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void updateMyLocation(Location myLocation) {
        if (mGoogleMap != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("You are Here");
            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 8.0f));
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
                PassengerRepository.getInstance().getUser().observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(@Nullable User user) {
                        mMainViewModel.createDrive(user, startLocation, endLocation);
                        updateMap(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()),
                                new LatLng(endLocation.getLatitude(), endLocation.getLongitude()));
                    }
                });
                break;
            case CreateRideDialogFragment.TYPE_REQUEST:
                final LifecycleOwner lifeCycleOwner = this;

                PassengerRepository.getInstance().getUser().observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(@Nullable User user) {
                        final DriveRequest driveRequest = mMainViewModel.createDriveRequest(user, startLocation, endLocation);
                        mMainViewModel.findBestDriveMatch(startLocation, endLocation).observe(lifeCycleOwner, new Observer<Drive>() {
                            @Override
                            public void onChanged(@Nullable Drive drive) {
                                if (drive != null) {
                                    mMainViewModel.addRequestDriveNotification(driveRequest, drive);
                                }
                            }
                        });
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

}
