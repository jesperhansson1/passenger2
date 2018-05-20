package com.cybercom.passenger.flows.main;

import android.Manifest;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createridefragment.CreateDriveFragment;
import com.cybercom.passenger.flows.driverconfirmation.AcceptRejectPassengerDialog;
import com.cybercom.passenger.flows.login.RegisterActivity;
import com.cybercom.passenger.flows.nomatchfragment.NoMatchFragment;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;
import com.cybercom.passenger.flows.progressfindingcar.FindingCarProgressDialog;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.route.FetchRouteUrl;
import com.cybercom.passenger.route.ParserTask;
import com.cybercom.passenger.service.Constants;
import com.cybercom.passenger.service.ForegroundServices;
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

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        CreateDriveFragment.CreateRideFragmentListener,
        AcceptRejectPassengerDialog.ConfirmationListener,
        PassengerNotificationDialog.PassengerNotificationListener,
        OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener,
        CreateDriveFragment.OnPlaceMarkerIconClickListener, ParserTask.OnRouteCompletion,
        CreateDriveFragment.OnFinishedCreatingDriveOrDriveRequest,
        FindingCarProgressDialog.FindingCarListener, GoogleMap.OnMyLocationButtonClickListener,
        NoMatchFragment.NoMatchButtonListener, FragmentSizeListener {

    private static final float ZOOM_LEVEL_STREETS = 15;

    private static final int DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED = 1500;
    private static final int PLACE_MARKER_INFO_FADE_DURATION = 1000;
    private static final float PLACE_MARKER_INFO_FADE_OUT_TO = 0.0f;
    private static final float PLACE_MARKER_INFO_FADE_IN_TO = 1.0f;
    private static final int ZOOM_LEVEL_MY_LOCATION = 17;
    private static final String DRIVE_ID = "driveId";

    private FirebaseUser mUser;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    private MainViewModel mMainViewModel;
    private TextView mPlaceMarkerInformation;

    private FragmentManager mFragmentManager;
    private CreateDriveFragment mCreateDriveFragment;

    private GoogleMap mGoogleMap;
    private Marker mStartLocationMarker;
    private HashMap<String, Marker> mPassengerMarkerMap = new HashMap<>();
    private HashMap<String, Marker> mDriverMarkerMap = new HashMap<>();
    private boolean isStartLocationMarkerAdded = false;

    private Marker mEndLocationMarker;
    private int mMarkerCount = 0;
    private boolean isEndLocationMarkerAdded = false;
    private Polyline mRoute;
    private Observer<Location> mEndLocationObserver;
    private Observer<Location> mStartLocationObserver;
    private LiveData<Drive> mFindMatch;
    private LiveData<Boolean> mTimer;
    private Observer<Drive> mMatchObserver;

    private boolean mIsFragmentAdded = false;
    private NoMatchFragment mNoMatchFragment;
    private boolean mCountMarker = true;

    public Bounds mBounds;

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
            mMainViewModel.getUser().observe(this, user -> Timber.i("User: %s logged in", user));
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
    }



    private void sendDriverPositionToDB(String driveId) {
        mMainViewModel.startLocationUpdates();
        mMainViewModel.getUpdatedLocationLiveData().observe(this, location -> {
            mMainViewModel.setCurrentLocationToDrive(driveId, location);
            mMainViewModel.setStartMarkerLocation(location);
        });
    }

    private void sendPassengerRideToDB(String driveId) {
        mMainViewModel.createPassengerRide(driveId).observe(this, passengerRide -> {
            mMainViewModel.startLocationUpdates();
            mMainViewModel.getUpdatedLocationLiveData().observe(this, location -> {
                mMainViewModel.updatePassengerRideCurrentLocation(location).observe(this, s -> {
                });
            });
        });
    }

    private void updateDriversMarkerPosition(String driveId) {
        mMainViewModel.getDriverPosition(driveId).observe(this, position -> {
            if (position == null) {
                return;
            }
            if (mDriverMarkerMap.containsKey(driveId)) {
                Marker driverMarker = mDriverMarkerMap.get(driveId);
                updateMarkerLocation(driverMarker,
                        LocationHelper.convertPositionToLocation(position));
            } else {
                LatLng startLatLng = new LatLng(position.getLatitude(),
                        position.getLongitude());

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(startLatLng)
                        .title(driveId)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker_location))
                        .anchor(0.5f, 0.5f)
                        .draggable(false));
                mDriverMarkerMap.put(driveId, marker);
            }
        });
    }

    private void updatePassengersMarkerPosition(String driveId){
        mMainViewModel.getPassengerRides(driveId).observe(
            this, passengerRide -> {
                if (passengerRide == null || passengerRide.getPassengerPos() == null) {
                    return;
                }

                String passengerRideId = passengerRide.getId();
                if (mPassengerMarkerMap.containsKey(passengerRideId)) {
                    Marker passengerMarker = mPassengerMarkerMap.get(passengerRideId);
                    updateMarkerLocation(passengerMarker,
                            LocationHelper.convertPositionToLocation(
                                    passengerRide.getPassengerPos()));
                } else {
                    LatLng startLatLng = new LatLng(passengerRide.getPassengerPos().getLatitude(),
                            passengerRide.getPassengerPos().getLongitude());

                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(startLatLng)
                            .title(getString(R.string.marker_title_passenger))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.passenger_loc))
                            .anchor(0.5f, 0.5f)
                            .draggable(false));
                    mPassengerMarkerMap.put(passengerRideId, marker);
                }
            });
    }

    private void initObservers() {
        mMainViewModel.getIncomingNotifications().observe(this, notification -> {
            if (notification == null) {
                return;
            }
            Timber.d("Notification to be displayed: %s", notification.toString());

            switch (notification.getType()) {
                case Notification.REQUEST_DRIVE:
                    showDriverConfirmationDialogFragment(notification);
                    break;
                case Notification.ACCEPT_PASSENGER:
                    showPassengerNotificationDialog(notification);
                    sendPassengerRideToDB(notification.getDrive().getId());
                    updateDriversMarkerPosition(notification.getDrive().getId());
                    dismissMatchingInProgressDialog();
                    break;
                case Notification.REJECT_PASSENGER:
                    matchDriveRequest(notification.getDriveRequest(),
                            mMainViewModel.getDriveRequestRadiusMultiplier());
                    mMainViewModel.getNextNotification(notification);
                    break;
            }
        });
    }

    // TODO Only start this when drive is started


        /*
        if (mLocation == null) {
            // Permission not granted to access user location
            Timber.i("get updated --> minc");
            setDefaultLocationToMinc();
        }*/

    private void placeStartLocationMarker() {

        if (!isStartLocationMarkerAdded) {
            if (mMainViewModel.getStartMarkerLocation().getValue() != null) {
                LatLng startLatLng = new LatLng(mMainViewModel.getStartMarkerLocation().getValue()
                        .getLatitude(),
                        mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

                mStartLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(startLatLng)
                                .title(getString(R.string.marker_title_start_location))
                                .icon(BitmapDescriptorFactory.fromResource(
                                        R.drawable.map_marker_start))
                                .draggable(true));
                animateToLocation(startLatLng);

                mMarkerCount++;
            }

            mStartLocationObserver = location -> {
                if (location != null) {
                    updateMarkerLocation(mStartLocationMarker, location);
                    if (isStartLocationMarkerAdded) {
                        mCreateDriveFragment.hideCreateDialog();
                        Handler handler = new Handler();
                        handler.postDelayed(() -> mCreateDriveFragment.showCreateDialog(),
                                DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED);
                    }
                    animateToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                    updateRoute();
                }
            };

            mMainViewModel.getStartMarkerLocation().observe(this, mStartLocationObserver);
            isStartLocationMarkerAdded = true;

        } else {
            updateMarkerLocation(mStartLocationMarker, mMainViewModel.getStartMarkerLocation().getValue());
            updateMarkerLocation(mStartLocationMarker,
                    mMainViewModel.getStartMarkerLocation().getValue());
        }
    }

    /**
     * Animates the camera to the given location
     *
     * @param locationToAnimateTo LatLng
     */
    private void animateToLocation(LatLng locationToAnimateTo) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToAnimateTo,
                ZOOM_LEVEL_STREETS));
    }

    private void placeEndLocationMarker() {

        if (!isEndLocationMarkerAdded) {
            LatLng endLatLng = new LatLng(0, 0);
            mEndLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title(getString(R.string.marker_title_end_location))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_end))
                    .draggable(true)
                    .visible(false));

            mEndLocationObserver = location -> {
                if (location != null) {
                    updateMarkerLocation(mEndLocationMarker, location);
                    mEndLocationMarker.setVisible(true);
                    if (mCountMarker) {
                        mMarkerCount++;
                        mCountMarker = false;
                    }
                    if (isEndLocationMarkerAdded) {
                        mCreateDriveFragment.hideCreateDialog();
                        Handler handler = new Handler();
                        handler.postDelayed(() -> mCreateDriveFragment.showCreateDialog(),
                                DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED);
                    }
                    animateToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
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

    private void matchDriveRequest(final DriveRequest driveRequest, int radiusMultiplier) {
        final LifecycleOwner lifecycleOwner = this;

        mFindMatch = mMainViewModel.findBestDriveMatch(driveRequest, radiusMultiplier);
        showMatchingInProgressDialog();
        mTimer = mMainViewModel.setFindMatchTimer();

        mMatchObserver = drive -> {
            if (drive != null) {
                Timber.i("matched drive %s", drive);
                mMainViewModel.addRequestDriveNotification(driveRequest, drive);
                if (mFindMatch != null) mFindMatch.removeObserver(mMatchObserver);
                if (mTimer != null) mTimer.removeObservers(lifecycleOwner);
            } else {
                Timber.i("No drives match for the moment. Keep listening.");
            }
        };

        mFindMatch.observe(lifecycleOwner, mMatchObserver);

        mTimer.observe(lifecycleOwner, aBoolean -> {
            showNoMatchDialog();
            cancelMatchingDrive();
        });
    }

    private void cancelMatchingDrive() {
        final LifecycleOwner lifecycleOwner = this;
        if (mFindMatch != null) {
            mFindMatch.removeObserver(mMatchObserver);
        }
        if (mTimer != null) {
            mTimer.removeObservers(lifecycleOwner);
        }
        dismissMatchingInProgressDialog();
    }

    private void showMatchingInProgressDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(
                FindingCarProgressDialog.MATCHING_IN_PROGRESS);

        if (fragment != null) {
            fragment.dismiss();
        }

        FindingCarProgressDialog findingCarProgressDialog = FindingCarProgressDialog.getInstance();
        findingCarProgressDialog.show(fragmentManager, FindingCarProgressDialog.MATCHING_IN_PROGRESS);
    }

    private void dismissMatchingInProgressDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(
                FindingCarProgressDialog.MATCHING_IN_PROGRESS);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    private void showNoMatchDialog() {
        mCreateDriveFragment.setIsOtherFragmentUp(true);
        mFragmentManager.beginTransaction()
                .add(R.id.main_activity_dialog_container, mNoMatchFragment).commit();
    }

    private void dismissNoMatchDialog() {
        mCreateDriveFragment.setIsOtherFragmentUp(false);
        mFragmentManager.beginTransaction().remove(mNoMatchFragment).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivity_login, menu);

        if (mUser != null) {
            menu.findItem(R.id.menu_action_login).setVisible(false);
        } else {
            menu.findItem(R.id.menu_action_login).setVisible(true);
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

    private void initUI() {
        mCreateDriveFragment = CreateDriveFragment.newInstance();
        mNoMatchFragment = NoMatchFragment.newInstance();

        mMainViewModel.getLastKnownLocation(location -> {
            mMainViewModel.setStartMarkerLocation(location);
            placeStartLocationMarker();
            mFragmentManager.beginTransaction()
                    .add(R.id.main_activity_dialog_container, mCreateDriveFragment).commit();
            mIsFragmentAdded = true;
        });

        mPlaceMarkerInformation = findViewById(R.id.main_activity_place_marker_info);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnCameraMoveStartedListener(this);
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);

        mGoogleMap.setMinZoomPreference(4.0f);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        //To show +/- zoom options
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.setOnMarkerDragListener(this);
        boolean fineLocationNotGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean coarseLocationNotGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;


        if (fineLocationNotGranted && coarseLocationNotGranted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            return;
        }
        initUI();
        mGoogleMap.setMyLocationEnabled(true);

        placeEndLocationMarker();

        if (!mMainViewModel.isInitialZoomDone()) {
            mMainViewModel.getLastKnownLocation(location -> {
                LatLng initialZoom = new LatLng(location.getLatitude(), location.getLongitude());
                animateToLocation(initialZoom);
                mMainViewModel.setInitialZoomDone(true);
            });
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void updateMarkerLocation(Marker marker, Location location) {
        if (mGoogleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            marker.setPosition(latLng);
        }
    }

    private void updateRoute() {
        if (mMarkerCount == 2) {
            if (mMainViewModel.getStartMarkerLocation().getValue() != null
                    && mMainViewModel.getEndMarkerLocation().getValue() != null) {

                if (mRoute != null) {
                    mRoute.remove();
                }

                LatLng origin = new LatLng(
                        mMainViewModel.getStartMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

                LatLng destination = new LatLng(
                        mMainViewModel.getEndMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getEndMarkerLocation().getValue().getLongitude());

                new FetchRouteUrl(mGoogleMap, origin, destination, this);


            }
        }
    }

    private void reRoute(LatLng origin, LatLng destination, LatLng viaStart, LatLng viaEnd)
    {
        if (mRoute != null) {
            mRoute.remove();
        }

        new FetchRouteUrl(mGoogleMap, origin, destination, viaStart, viaEnd, this);

    }

    private void zoomToFitRoute() {
        final Handler handler = new Handler();
       //handler.postDelayed(() -> {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mStartLocationMarker.getPosition());
            builder.include(mEndLocationMarker.getPosition());
            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (height * 0.15);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height,
                    padding);

            mGoogleMap.animateCamera(cameraUpdate);
       // }, DELAY_BEFORE_ZOOM_TO_FIT_ROUTE);

    }

    private void showDriverConfirmationDialogFragment(Notification notification) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AcceptRejectPassengerDialog dialogFragment = (AcceptRejectPassengerDialog)
                fragmentManager.findFragmentByTag(AcceptRejectPassengerDialog.TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }

        AcceptRejectPassengerDialog dFragment = AcceptRejectPassengerDialog.getInstance(
                notification);
        dFragment.show(getSupportFragmentManager(), AcceptRejectPassengerDialog.TAG);
    }

    private void showPassengerNotificationDialog(Notification notification) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        PassengerNotificationDialog dialogFragment = (PassengerNotificationDialog)
                fragmentManager.findFragmentByTag(PassengerNotificationDialog.TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }

        PassengerNotificationDialog dFragment = PassengerNotificationDialog.getInstance(
                notification);
        dFragment.show(getSupportFragmentManager(), PassengerNotificationDialog.TAG);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
                && mIsFragmentAdded) {
            mCreateDriveFragment.hideCreateDialog();
            Timber.i("onCameraMoveStarted");
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mMainViewModel.getWhichMarkerToAdd() == MainViewModel.PLACE_START_MARKER) {
            mMainViewModel.setStartMarkerLocation(LocationHelper.convertLatLngToLocation(latLng));
            placeStartLocationMarker();
        }
        if (mMainViewModel.getWhichMarkerToAdd() == MainViewModel.PLACE_END_MARKER) {
            mMainViewModel.setEndMarkerLocation(LocationHelper.convertLatLngToLocation(latLng));
            placeEndLocationMarker();
        }

        hidePlaceMarkerInformation();
        mCreateDriveFragment.showCreateDialog();
    }

    @Override
    public void onDriverConfirmation(boolean isAccepted, Notification notification) {
        if (isAccepted) {
            mMainViewModel.sendAcceptPassengerNotification(notification.getDrive(),
                    notification.getDriveRequest());
        } else {
            mMainViewModel.sendRejectPassengerNotification(notification.getDrive(),
                    notification.getDriveRequest());
        }
        mMainViewModel.getNextNotification(notification);
    }

    @Override
    public void onCancelDrive(Notification notification) {
        mMainViewModel.getNextNotification(notification);
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
            Location markerLocation = new Location(getString(
                    R.string.location_provider_marker_drag));
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            mMainViewModel.setStartMarkerLocation(markerLocation);
        }

        if (marker.getTitle().equals(getString(R.string.marker_title_end_location))) {
            Location markerLocation = new Location(getString(
                    R.string.location_provider_marker_drag));
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            mMainViewModel.setEndMarkerLocation(markerLocation);
        }

        hidePlaceMarkerInformation();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mCreateDriveFragment.hideCreateDialog();
    }

    @Override
    public void onRouteDrawn(Polyline route) {
        mRoute = route;
        zoomToFitRoute();
    }

    public void onBoundsParse(Bounds bounds)
    {
        if(bounds!=null) {
            mBounds = bounds;
        }
        else
        {
            mBounds = new Bounds(0.0,0.0,0.0,0.0);
        }
    }

    @Override
    public void onPlaceMarkerIconClicked() {

        switch (mMainViewModel.getWhichMarkerToAdd()) {
            case MainViewModel.PLACE_START_MARKER: {
                mPlaceMarkerInformation.setText(R.string.place_start_marker_information_text);
                break;
            }
            case MainViewModel.PLACE_END_MARKER: {
                mPlaceMarkerInformation.setText(R.string.place_end_marker_information_text);
                break;
            }
        }

        showPlaceMarkerInformation();
        mCreateDriveFragment.hideCreateDialog();
    }

    private void hidePlaceMarkerInformation() {
        mPlaceMarkerInformation.animate().alpha(PLACE_MARKER_INFO_FADE_OUT_TO)
                .setDuration(PLACE_MARKER_INFO_FADE_DURATION);
    }

    private void showPlaceMarkerInformation() {
        mPlaceMarkerInformation.animate().alpha(PLACE_MARKER_INFO_FADE_IN_TO)
                .setDuration(PLACE_MARKER_INFO_FADE_DURATION);
    }

    @Override
    public void onFinish() {
        mGoogleMap.clear();
        isStartLocationMarkerAdded = false;
        isEndLocationMarkerAdded = false;
        mMainViewModel.getStartMarkerLocation().removeObserver(mStartLocationObserver);
        mMainViewModel.getEndMarkerLocation().removeObserver(mEndLocationObserver);

        mMarkerCount = 0;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean fineLocationNotGranted = ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED;
                    boolean coarseLocationNotGranted = ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED;

                    if (fineLocationNotGranted && coarseLocationNotGranted) {
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
                            LatLng initialZoom = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            animateToLocation(initialZoom);
                            mMainViewModel.setInitialZoomDone(true);
                        });
                    }
                    initUI();

                } else {

                    // permission denied
                    // Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onCreateRide(long time, int type, Position startLocation, Position endLocation,
                             int seats) {
        mCreateDriveFragment.hideCreateDialog();
        switch (type) {
            case User.TYPE_DRIVER:
                if(mBounds==null) {
                    mBounds = new Bounds(0.0,0.0,0.0,0.0);
                }
                mMainViewModel.createDrive(time, startLocation, endLocation, seats, mBounds).observe(this,
                        drive -> {
                    if (drive != null) {
                        Intent UpdateDriveIntent = new Intent(MainActivity.this, ForegroundServices.class);
                        UpdateDriveIntent.setAction(Constants.ACTION.STARTFOREGROUND_UPDATE_DRIVER_POSITION);
                        UpdateDriveIntent.putExtra(DRIVE_ID, drive.getId());
                        startService(UpdateDriveIntent);
                        /*sendDriverPositionToDB(drive.getId());
                        updatePassengersMarkerPosition(drive.getId());
                        Timber.i("Drive created: %s", drive.getId());*/
                        mCreateDriveFragment.setDefaultValuesToDialog();
                    }
                });
                break;
            case User.TYPE_PASSENGER:
                mMainViewModel.createDriveRequest(time, startLocation, endLocation, seats).observe(
                        this, driveRequest -> {
                    mMainViewModel.setDriveRequestRadiusMultiplier(
                            MainViewModel.DRIVE_REQUEST_DEFAULT_MULTIPLIER);
                    Timber.i("DriveRequest : %s", driveRequest);
                    matchDriveRequest(driveRequest,
                            mMainViewModel.getDriveRequestRadiusMultiplier());
                    mCreateDriveFragment.setDefaultValuesToDialog();
                });
                break;
        }
    }

    @Override
    public void onCancelFindingCarPressed(Boolean isCancelPressed) {
        cancelMatchingDrive();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL_MY_LOCATION));
        return false;
    }

    @Override
    public void OnNoMatchButtonClicked(int type) {
        switch (type) {
            case NoMatchFragment.BUTTON_TRY_AGAIN: {
                matchDriveRequest(mMainViewModel.getMostRecentDriveRequest(),
                        mMainViewModel.getDriveRequestRadiusMultiplier());
                dismissNoMatchDialog();
                break;
            }
            case NoMatchFragment.BUTTON_INCREASE_RADIUS: {
                mMainViewModel.setDriveRequestRadiusMultiplier(
                        mMainViewModel.getDriveRequestRadiusMultiplier()
                                + MainViewModel.DRIVE_REQUEST_INCREASE_MULTIPLIER_BY_ONE);
                matchDriveRequest(mMainViewModel.getMostRecentDriveRequest(),
                        mMainViewModel.getDriveRequestRadiusMultiplier());
                dismissNoMatchDialog();
                break;
            }
            case NoMatchFragment.BUTTON_CANCEL: {
                dismissNoMatchDialog();
                break;
            }
        }
    }

    @Override
    public void onHeightChanged(int fragmentHeight) {
        mGoogleMap.setPadding(0,0,0,fragmentHeight);
        if (mMarkerCount == 1) {
            animateToLocation(mStartLocationMarker.getPosition());
        } else {
            zoomToFitRoute();
        }
    }
}