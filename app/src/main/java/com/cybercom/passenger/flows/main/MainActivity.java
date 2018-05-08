package com.cybercom.passenger.flows.main;

import android.Manifest;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialogFragment;
import com.cybercom.passenger.flows.createridefragment.CreateDriveFragment;
import com.cybercom.passenger.flows.driverconfirmation.AcceptRejectPassengerDialog;
import com.cybercom.passenger.flows.login.RegisterActivity;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.route.FetchRouteUrl;
import com.cybercom.passenger.route.ParserTask;
import com.cybercom.passenger.utils.LocationHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CreateRideDialogFragment.CreateRideDialogFragmentListener, AcceptRejectPassengerDialog.ConfirmationListener, PassengerNotificationDialog.PassengerNotificationListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener, CreateDriveFragment.OnPlaceMarkerIconClickListener, ParserTask.OnRouteCompletion, CreateDriveFragment.OnFinishedCreatingDriveOrDriveRequest {

    private static final float ZOOM_LEVEL_WORLD = 1;
    private static final float ZOOM_LEVEL_LANDMASS_CONTINENT = 5;
    private static final float ZOOM_LEVEL_CITY = 10;
    private static final float ZOOM_LEVEL_STREETS = 15;
    private static final float ZOOM_LEVEL_BUILDINGS = 20;

    private static final String TAG = "complete";
    public static final int DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED = 2000;
    public static final int DELAY_BEFORE_ZOOM_TO_FIT_ROUTE = 1500;
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
    private boolean isStartLocationMarkerAdded = false;
    private int mMarkerCount = 0;
    private boolean isEndLocationMarkerAdded = false;
    private Polyline mRoute;
    private Observer<Location> endLocationObserver;
    private Observer<Location> mEndLocationObserver;
    private Observer<Location> mStartLocationObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.i("First log info");
        Fabric.with(this, new Crashlytics());

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().get("driveId") != null) {
                    mMainViewModel.setIncomingNotification(getIntent().getExtras());
                }
            }
        }
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            mMainViewModel.refreshToken(FirebaseInstanceId.getInstance().getToken());

            mMainViewModel.getUser().observe(this, new Observer<User>() {
                @Override
                public void onChanged(@Nullable User user) {
                    Timber.i("User: %s logged in", user);
                    if (user != null) {
                        Toast.makeText(getApplicationContext(),user.getAccId(),Toast.LENGTH_LONG).show();
                        if (user.getType() == User.TYPE_DRIVER) {
                            setUpForDriver();
                        } else {
                            setUpForPassenger();
                        }
                    }
                }
            });
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.mainactivity_title);
            }

            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_activitymap_googlemap);
        mapFragment.getMapAsync(this);

        initObservers();
        initUI();

    }

    private void setUpForDriver() {
        mFloatRide.setImageResource(R.drawable.driver_floating_button);
    }

    private void setUpForPassenger() {
        mFloatRide.setImageResource(R.drawable.passenger);
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

    private void placeStartLocationMarker() {

        if (!isStartLocationMarkerAdded) {
            if (mMainViewModel.getStartMarkerLocation().getValue() != null) {
                LatLng startLatLng = new LatLng(mMainViewModel.getStartMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

                mStartLocationMarker =
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(startLatLng)
                                .title(getString(R.string.marker_title_start_location))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker_location))
                                .anchor(0.5f, 0.5f)
                                .draggable(true));
                animateToLocation(startLatLng, ZOOM_LEVEL_STREETS);

                mMarkerCount++;
            }

            mStartLocationObserver = location -> {
                if (location != null) {
                    updateMarkerLocation(mStartLocationMarker, location);
                    if (isStartLocationMarkerAdded) {
                        hideFragmentAnimation(mCreateDriveFragment);
                        Handler handler = new Handler();
                        handler.postDelayed(() -> showFragment(mCreateDriveFragment), DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED);
                    }
                    animateToLocation(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL_STREETS);
                    updateRoute();
                }
            };

            mMainViewModel.getStartMarkerLocation().observe(this, mStartLocationObserver);

            isStartLocationMarkerAdded = true;

        } else {
            updateMarkerLocation(mStartLocationMarker, mMainViewModel.getStartMarkerLocation().getValue());

        }
    }

    /**
     * Animates the camera to the given location
     *
     * @param locationToAnimateTo LatLng
     * @param zoomLevel           float with desired zoom level
     */
    private void animateToLocation(LatLng locationToAnimateTo, float zoomLevel) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToAnimateTo, zoomLevel));
    }

    private void placeEndLocationMarker() {

        if (!isEndLocationMarkerAdded) {
            LatLng endLatLng = new LatLng(0, 0);
            mEndLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title(getString(R.string.marker_title_end_location))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_end_location))
                    .anchor(0.5f, 0.5f)
                    .draggable(true)
                    .visible(false));

            mMarkerCount++;
            mEndLocationObserver = location -> {
                if (location != null) {
                    updateMarkerLocation(mEndLocationMarker, location);
                    mEndLocationMarker.setVisible(true);
                    if (isEndLocationMarkerAdded) {
                        hideFragmentAnimation(mCreateDriveFragment);
                        Handler handler = new Handler();
                        handler.postDelayed(() -> showFragment(mCreateDriveFragment), DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED);
                    }
                    animateToLocation(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL_STREETS);
                    updateRoute();
                }
            };

            mMainViewModel.getEndMarkerLocation().observe(this, mEndLocationObserver);
            isEndLocationMarkerAdded = true;
        } else {
            mEndLocationMarker.setVisible(true);
            updateMarkerLocation(mEndLocationMarker, mMainViewModel.getEndMarkerLocation().getValue());
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
        // mMainViewModel.startLocationUpdates();
    }

    public void initUI() {
        mFloatRide = findViewById(R.id.button_createRide);
        mFloatRide.setImageResource(R.drawable.passenger);
        mCreateDriveFragment = CreateDriveFragment.newInstance();
        mFloatRide.setOnClickListener(view -> {
            showFragment(mCreateDriveFragment);
            if (!isStartLocationMarkerAdded) {
                mMainViewModel.getLastKnownLocation(location -> {
                    mMainViewModel.setStartMarkerLocation(location);
                    placeStartLocationMarker();
                });
            }
            mFloatRide.setVisibility(View.INVISIBLE);
            isCreateDriveFragmentVisible = true;
        });


    }


    private Boolean isFragmentAdded = false;

    public void showFragment(Fragment fragment) {
        if (isFragmentAdded) {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_enter_animation, R.anim.dialog_exit_animation)
                    .show(fragment).commit();
            isCreateDriveFragmentVisible = true;
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
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnMapClickListener(this);

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


        placeEndLocationMarker();

        if (!mMainViewModel.isInitialZoomDone()) {
            mMainViewModel.getLastKnownLocation(location -> {
                LatLng initialZoom = new LatLng(
                        location.getLatitude(),
                        location.getLongitude());

                animateToLocation(initialZoom, ZOOM_LEVEL_STREETS);
                mMainViewModel.setInitialZoomDone(true);
            });
        }
    }


    public void hideFragmentAnimation(final Fragment fragment) {
        if (isCreateDriveFragmentVisible) {
            mFloatRide.setVisibility(View.VISIBLE);

            if (fragment != null) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
                        R.anim.dialog_exit_animation);

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


                findViewById(R.id.create_drive_dialog).startAnimation(animation);
            }

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void updateMarkerLocation(Marker marker, Location location) {
        if (mGoogleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            marker.setPosition(latLng);
        }
    }

    public void updateRoute() {
        if (mMarkerCount == 2) {
            if (mMainViewModel.getStartMarkerLocation().getValue() != null
                    && mMainViewModel.getEndMarkerLocation().getValue() != null) {

                if (mRoute != null) {
                    mRoute.remove();
                }

                LatLng origin = new LatLng(mMainViewModel.getStartMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

                LatLng destination = new LatLng(mMainViewModel.getEndMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getEndMarkerLocation().getValue().getLongitude());

                new FetchRouteUrl(mGoogleMap, origin, destination, this);

            }
        }
    }

    public void zoomToFitRoute() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mStartLocationMarker.getPosition());
            builder.include(mEndLocationMarker.getPosition());
            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (height * 0.15);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            mGoogleMap.animateCamera(cameraUpdate);
        }, DELAY_BEFORE_ZOOM_TO_FIT_ROUTE);

    }

    @Override
    public void onCreateRide(int type, final Position startLocation, final Position endLocation) {
        Timber.i("on create ride");

        switch (type) {
            case CreateRideDialogFragment.TYPE_RIDE:
                long time = System.currentTimeMillis();
                int availableSeats = 4;

                mMainViewModel.createDrive(time, startLocation, endLocation, availableSeats).observe(this, new Observer<Drive>() {
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
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
                && isFragmentAdded) {
            hideFragmentAnimation(mCreateDriveFragment);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mMainViewModel.getWhichMarkerToAdd() == MainViewModel.PLACE_START_MARKER) {
            mMainViewModel.setStartMarkerLocation(LocationHelper.convertLatLngToLocation(latLng));
            placeStartLocationMarker();
            showFragment(mCreateDriveFragment);
            mGoogleMap.setOnMapClickListener(this);

        }

        if (mMainViewModel.getWhichMarkerToAdd() == MainViewModel.PLACE_END_MARKER) {
            mMainViewModel.setEndMarkerLocation(LocationHelper.convertLatLngToLocation(latLng));
            placeEndLocationMarker();
            showFragment(mCreateDriveFragment);
        }
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
            Location markerLocation = new Location(getString(R.string.location_provider_marker_drag));
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            mMainViewModel.setStartMarkerLocation(markerLocation);
        }

        if (marker.getTitle().equals(getString(R.string.marker_title_end_location))) {
            Location markerLocation = new Location(getString(R.string.location_provider_marker_drag));
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            mMainViewModel.setEndMarkerLocation(markerLocation);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideFragmentAnimation(mCreateDriveFragment);
    }

    @Override
    public void onRouteDrawn(Polyline route) {
        mRoute = route;
        zoomToFitRoute();
    }

    @Override
    public void onPlaceMarkerIconClicked() {
        hideFragmentAnimation(mCreateDriveFragment);
    }

    @Override
    public void onFinish() {
        hideFragmentAnimation(mCreateDriveFragment);
        removeFragment();
        mGoogleMap.clear();
        isFragmentAdded = false;
        isStartLocationMarkerAdded = false;
        isEndLocationMarkerAdded = false;
        isCreateDriveFragmentVisible = true;
        mMainViewModel.getEndMarkerLocation().removeObserver(mEndLocationObserver);
        mMainViewModel.setEndMarker(new MutableLiveData<>());
        mMainViewModel.setEndAddress(new MutableLiveData<>());
        mMarkerCount = 0;
        mMainViewModel.getStartMarkerLocation().removeObserver(mStartLocationObserver);
    }

    public void removeFragment() {
        getSupportFragmentManager().beginTransaction().remove(mCreateDriveFragment).commit();
        mCreateDriveFragment = CreateDriveFragment.newInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mGoogleMap.setMyLocationEnabled(true);

                    if (!mMainViewModel.isInitialZoomDone()) {
                        mMainViewModel.getLastKnownLocation(location -> {
                            LatLng initialZoom = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude());

                            animateToLocation(initialZoom, ZOOM_LEVEL_STREETS);
                            mMainViewModel.setInitialZoomDone(true);
                        });
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
