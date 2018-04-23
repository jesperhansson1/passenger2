package com.cybercom.passenger.flows.main;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialogFragment;
import com.cybercom.passenger.flows.driverconfirmation.AcceptRejectPassengerDialog;
import com.cybercom.passenger.flows.login.LoginActivity;
import com.cybercom.passenger.flows.login.RegisterActivity;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.route.FetchRouteUrl;
import com.cybercom.passenger.utils.LocationHelper;
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

        mMainViewModel.getUpdatedLocationLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                // TODO: Need to handle if there is no data och display info. Need to send this location to spinner
                if(location == null) {
                    Timber.d("get updated --> null");
                }else {
                    mLocation = location;
                }
            }
        });
        if (mLocation == null)
        {
            //Permission not granted to access user location
            setDefaultLocationToMinc();
        }
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

    public void setDefaultLocationToMinc()
    {
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
            Intent intent = new Intent(this, RegisterActivity.class);
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

    public void showCreateDriveDialog(int type, String location) {
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
        updateMyLocation(mLocation);

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

}
