package com.cybercom.passenger.flows.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.BuildConfig;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createridefragment.CreateDriveFragment;
import com.cybercom.passenger.flows.driverconfirmation.AcceptRejectPassengerDialog;
import com.cybercom.passenger.flows.dropofffragment.DriverDropOffFragment;
import com.cybercom.passenger.flows.login.RegisterActivity;
import com.cybercom.passenger.flows.nomatchfragment.NoMatchFragment;
import com.cybercom.passenger.flows.passengernotification.PassengerNotificationDialog;
import com.cybercom.passenger.flows.pickupfragment.DriverPassengerPickUpFragment;
import com.cybercom.passenger.flows.progressfindingcar.FindingCarProgressDialog;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.PassengerRide;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.route.FetchRoute;
import com.cybercom.passenger.route.ParserTask;
import com.cybercom.passenger.route.RidePoints;
import com.cybercom.passenger.route.Route;
import com.cybercom.passenger.service.Constants;
import com.cybercom.passenger.service.ForegroundServices;
import com.cybercom.passenger.service.GeofenceTransitionsIntentService;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.NotificationHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        NoMatchFragment.NoMatchButtonListener, FragmentSizeListener, OnCompleteListener<Void>,
        DriverPassengerPickUpFragment.DriverPassengerPickUpButtonClickListener,
        DriverDropOffFragment.DriverDropOffConfirmationListener, View.OnClickListener {

    private static final float ZOOM_LEVEL_STREETS = 15;

    private static final int DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED = 1500;
    private static final int PLACE_MARKER_INFO_FADE_DURATION = 1000;
    private static final float PLACE_MARKER_INFO_FADE_OUT_TO = 0.0f;
    private static final float PLACE_MARKER_INFO_FADE_IN_TO = 1.0f;
    private static final int ZOOM_LEVEL_MY_LOCATION = 17;
    private static final String DRIVE_ID = "driveId";
    private static final String PASSENGER_RIDE_KEY = "passengerRideKey";
    public static final int GEOFENCE_RADIUS = 50;
    public static final int GEOFENCE_TIME_OUT = 1000 * 60 * 60 * 24;
    public static final String GEOFENCE_EVENTS_INTENT_FILTER = "GEOFENCE_EVENTS";
    public static final String GEOFENCE_EVENTS_REQUEST_ID = "GEOFENCE_EVENTS_REQUEST_ID";
    private static final String GEOFENCE_TYPE_PICK_UP = "1";
    private static final String GEOFENCE_TYPE_DROP_OFF = "2";
    public static final String FOREGROUND_SERVICE_INTENT_FILTER = "FOREGROUND_SERVICE";
    public static final int TYPE_PICK_UP = 1;
    public static final int TYPE_DROP_OFF = 2;
    public static final String DIALOG_TO_SHOW = "DIALOG_TO_SHOW";
    private static final int ROUTE_WIDTH = 10;
    private static final int ROUTE_COLOR = Color.rgb(6, 182, 239);


    private FirebaseUser mUser;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    private MainViewModel mMainViewModel;
    private TextView mPlaceMarkerInformation;
    private FloatingActionButton mCancelDriveFab;
    private Button mConfirmCancelDrive;

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
    private LinearLayout mPassengerContainer;
    private FrameLayout mPassengerDetailedInformation;

    private GeofencingClient mGeofencingClient;
    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private List<PassengerRide> mPassengerRides;

    private GeofenceBroadcastReceiver mGeofenceReceiver;
    private ForegroundServiceReceiver mForegroundReceiver;

    private User mCurrentLoggedInUser;

    private HashMap<String, PassengerRide> mPassengers = new HashMap<>();

    private Bounds mBounds;
    private List<String> mActiveDriveIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!BuildConfig.DEBUG) {
            Timber.i("This is the release version");
            Fabric.with(this, new Crashlytics());
        } else {
            Timber.i("This is the debug version");
        }

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mFragmentManager = getSupportFragmentManager();
        createNotificationChannels();

        mGeofenceReceiver = new GeofenceBroadcastReceiver();
        mForegroundReceiver = new ForegroundServiceReceiver();

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
            mMainViewModel.getUser().observe(this, user -> {
                Timber.i("User: %s logged in", user);
                mCurrentLoggedInUser = user;
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
        setUpGeofencing();
        initObservers();
    }

    private void createNotificationChannels() {
        new NotificationHelper(this);
    }

    private void createPassengerRide(Drive drive, Position startPosition, Position endPosition,
                                     String startAddress, String endAddress) {
        mMainViewModel.createPassengerRide(drive, startPosition, endPosition, startAddress,
                endAddress).observe(this, passengerRide -> {
            //reroute here wrong

            Intent updatePassengerIntent = new Intent(MainActivity.this, ForegroundServices.class);
            updatePassengerIntent.setAction(
                    Constants.ACTION.STARTFOREGROUND_UPDATE_PASSENGER_POSITION);
            updatePassengerIntent.putExtra(ForegroundServices.INTENT_EXTRA_PASSENGER_RIDE_ID,
                    passengerRide.getId());
            updatePassengerIntent.putExtra(ForegroundServices.INTENT_EXTRA_DRIVE_ID,
                    passengerRide.getDrive().getId());
            if (passengerRide != null) {
                updatePassengerIntent.putExtra(PASSENGER_RIDE_KEY, passengerRide.getId());
            }
            startService(updatePassengerIntent);
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

    private void updatePassengersMarkerPosition(String driveId) {
        mMainViewModel.getPassengerRides(driveId).observe(
                this, passengerRide -> {
                    if (passengerRide == null) {
                        return;
                    }

                    observePassengersPosition(passengerRide.getPassenger().getUserId());
                });
    }

    private void observePassengersPosition(final String passengerId) {
        mMainViewModel.getPassengerPosition(passengerId).observe(this, position -> {
            Timber.d("position update");

            if (mPassengerMarkerMap.containsKey(passengerId)) {
                Marker passengerMarker = mPassengerMarkerMap.get(passengerId);
                updateMarkerLocation(passengerMarker,
                        LocationHelper.convertPositionToLocation(
                                position));
            } else {
                LatLng startLatLng = new LatLng(position.getLatitude(),
                        position.getLongitude());

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(startLatLng)
                        .title(getString(R.string.marker_title_passenger))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.passenger_loc))
                        .anchor(0.5f, 0.5f)
                        .draggable(false));
                mPassengerMarkerMap.put(passengerId, marker);
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
                    String startAddress = mMainViewModel.getAddressFromLocation(
                            LocationHelper.convertPositionToLocation(notification.getDriveRequest()
                                    .getStartLocation()));
                    String endAddress = mMainViewModel.getAddressFromLocation(
                            LocationHelper.convertPositionToLocation(
                                    notification.getDriveRequest().getEndLocation()));
                    createPassengerRide(notification.getDrive(),
                            notification.getDriveRequest().getStartLocation(),
                            notification.getDriveRequest().getEndLocation(), startAddress,
                            endAddress);
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

        final LifecycleOwner lifecycleOwner = this;
        mPassengerRides = new ArrayList<>();

        mMainViewModel.getActiveDriveId().observe(this, driveId -> {
            if (driveId == null) {
                // TODO: No drive is active or drive has been cancelled. Update UI accordingly.
            } else {
                if (mActiveDriveIdList.contains(driveId)) {
                    // Make sure to only observe once
                    Timber.d("Have already added observer");
                    return;
                }

                if (mActiveDriveIdList.size() > 1) {
                    Toast.makeText(this, "Error: More than one drive active " +
                            "for the current user", Toast.LENGTH_LONG).show();
                }

                mActiveDriveIdList.add(driveId);
                mMainViewModel.getPassengerRides(driveId).observe(lifecycleOwner, passengerRide -> {
                    if (passengerRide != null) {
                        if (isPassengerRideAlreadyAddedToLocalList(passengerRide)) {
                            replacePassengerRide(passengerRide);
                        } else {
                            mPassengerRides.add(passengerRide);
                            createGeofence(passengerRide);
                        }
                    }
                    //reroute here wrong
                    handlePassengerChanged(passengerRide);

                });
            }
        });
    }

    private void replacePassengerRide(PassengerRide passengerRide) {
        int passengerRideToReplace = -1;
        for (int i = 0; i < mPassengerRides.size(); i++) {
            if (passengerRide.getId().equals(mPassengerRides.get(i).getId())) {
                passengerRideToReplace = i;
            }
        }
        if (passengerRideToReplace != -1) {
            mPassengerRides.remove(passengerRideToReplace);
        }
        mPassengerRides.add(passengerRide);
    }

    private boolean isPassengerRideAlreadyAddedToLocalList(PassengerRide passengerRide) {
        for (PassengerRide passengerRideFromList : mPassengerRides) {
            if (passengerRide.getId().equals(passengerRideFromList.getId())) {
                return true;
            }
        }
        return false;
    }

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
            updateMarkerLocation(mEndLocationMarker, mMainViewModel.getEndMarkerLocation()
                    .getValue());
        }
    }

    private void matchDriveRequest(final DriveRequest driveRequest, int radiusMultiplier) {
        final LifecycleOwner lifecycleOwner = this;
        String googleApiKey = getResources().getString(R.string.google_api_key);

        mFindMatch = mMainViewModel.findBestDriveMatch(driveRequest, radiusMultiplier, googleApiKey);
        showMatchingInProgressDialog();
        mTimer = mMainViewModel.setFindMatchTimer();

        mMatchObserver = drive -> {
            if (drive != null) {
                Timber.i("matched drive %s", drive);
                mMainViewModel.addRequestDriveNotification(driveRequest, drive);
                if (mFindMatch != null) {
                    mFindMatch.removeObserver(mMatchObserver);
                }
                if (mTimer != null) {
                    mTimer.removeObservers(lifecycleOwner);
                }
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
        findingCarProgressDialog.show(fragmentManager,
                FindingCarProgressDialog.MATCHING_IN_PROGRESS);
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
        menu.findItem(R.id.menu_action_login).setVisible(mUser == null);
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
            if (location != null) {
                mMainViewModel.setStartMarkerLocation(location);
                placeStartLocationMarker();
                mFragmentManager.beginTransaction()
                        .add(R.id.main_activity_dialog_container, mCreateDriveFragment).commit();
                mIsFragmentAdded = true;
            }
        });

        mPlaceMarkerInformation = findViewById(R.id.main_activity_place_marker_info);
        mPassengerContainer = findViewById(R.id.passenger_container);
        mPassengerDetailedInformation = findViewById(R.id.passenger_detailed_information);
        mPassengerDetailedInformation.findViewById(R.id.abort_passenger_button)
                .setOnClickListener(this);
        mCancelDriveFab = findViewById(R.id.cancel_drive);
        mCancelDriveFab.setOnClickListener(this);
        mConfirmCancelDrive = findViewById(R.id.confirm_cancel_drive_button);
        mConfirmCancelDrive.setOnClickListener(this);
    }

    private void handlePassengerChanged(PassengerRide passengerRide) {
        if (passengerRide == null) {
            return;
        }
        if (passengerRide.isDropOffConfirmed()) {
            mPassengers.remove(passengerRide.getId());
            removePassengerFab(passengerRide.getId());
        } else if (mPassengers.get(passengerRide.getId()) == null) {
                mPassengers.put(passengerRide.getId(), passengerRide);
                addPassengerFab(passengerRide.getId());
        }
        Drive drive = passengerRide.getDrive();
        Position driveStartLocation = drive.getStartLocation();
        Position driveEndLocation = drive.getEndLocation();
        List<RidePoints> ridePointsList = new ArrayList();
        ridePointsList.add(createRidePointsFromPassengerRide(passengerRide));
        for (PassengerRide onBordedPassenger : mPassengers.values()) {
            ridePointsList.add(createRidePointsFromPassengerRide(onBordedPassenger));
        }
        reRoute(createLatLngFromPosition(driveStartLocation),
                createLatLngFromPosition(driveEndLocation), ridePointsList);
    }


    private void addPassengerFab(@NonNull String rideId) {

        if (findPassengerView(rideId) == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            Resources resources = getResources();
            int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    R.dimen.pad_16dp, resources.getDisplayMetrics());

            lp.setMargins(margin, margin, margin, margin);
            LinearLayout fabContainer = (LinearLayout)
                    layoutInflater.inflate(R.layout.passanger_fab, null);
            FloatingActionButton fab = fabContainer.findViewById(R.id.passenger_fab);
            fab.setOnClickListener(this);
            fab.setTag(rideId);
            mPassengerContainer.addView(fabContainer);
        }
    }

    private void removePassengerFab(@NonNull String rideId) {
        View child = findPassengerView(rideId);
        if (child != null) {
            mPassengerContainer.removeView(child);
        }
    }

    private View findPassengerView(@NonNull String rideId) {
        View child;
        for (int i = 0; i < mPassengerContainer.getChildCount(); i++) {
            child = mPassengerContainer.getChildAt(i);
            if (rideId.equals(child.getTag())) {
                return child;
            }
        }
        return null;
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
                if (location != null) {
                    LatLng initialZoom = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    animateToLocation(initialZoom);
                    mMainViewModel.setInitialZoomDone(true);
                }
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

                new FetchRoute(origin, destination, null, this);
            }
        }
    }

    private void reRoute(LatLng origin, LatLng destination, List<RidePoints> wayPoints) {
        if (mRoute != null) {
            mRoute.remove();
        }
        new FetchRoute(origin, destination, wayPoints, this);
    }

    private void zoomToFitRoute() {
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

    private void removePassengerNotificationDialog() {
        PassengerNotificationDialog dFragment = (PassengerNotificationDialog)
                mFragmentManager.findFragmentByTag(PassengerNotificationDialog.TAG);
        mFragmentManager.beginTransaction().remove(dFragment).commit();
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE && mIsFragmentAdded) {
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

    private RidePoints createRidePointsFromPassengerRide(@NonNull PassengerRide passengerRide) {
        LatLng pickUp = createLatLngFromPosition(passengerRide.getPickUpPosition());
        LatLng dropOff = createLatLngFromPosition(passengerRide.getDropOffPosition());
        return new RidePoints(pickUp, dropOff);
    }

    private LatLng createLatLngFromPosition(@NonNull Position position) {
        return new LatLng(position.getLatitude(), position.getLongitude());
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
        if (mIsFragmentAdded) {
            mCreateDriveFragment.hideCreateDialog();
        }
    }

    @Override
    public void onRoutesFetched(List<Route> routes) {
        List<LatLng> polylinePoints = new ArrayList<>();
        for (Route route : routes) {
            //TODO can we have many routes??
            Bounds bounds = route.getBounds();
            if (bounds != null) {
                mBounds = bounds;
            } else {
                mBounds = new Bounds(0.0, 0.0, 0.0, 0.0, 0, 0);
            }
            polylinePoints.addAll(route.getPolyline());
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(polylinePoints);
        polylineOptions.width(ROUTE_WIDTH);
        polylineOptions.color(ROUTE_COLOR);

        mRoute = mGoogleMap.addPolyline(polylineOptions);
        zoomToFitRoute();
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
        removeCreateDriveFragment();

     /*  mGoogleMap.clear();
        isStartLocationMarkerAdded = false;
        isEndLocationMarkerAdded = false;
        mMainViewModel.getStartMarkerLocation().removeObserver(mStartLocationObserver);
        mMainViewModel.getEndMarkerLocation().removeObserver(mEndLocationObserver);

        mMarkerCount = 0;
        mCountMarker = true;*/


    }

    private void removeCreateDriveFragment() {
        mIsFragmentAdded = false;
        mFragmentManager.beginTransaction().remove(mCreateDriveFragment).commit();
    }

    private void addCreateDriveFragment() {
        mIsFragmentAdded = true;
        mFragmentManager.beginTransaction().add(R.id.main_activity_dialog_container,
                mCreateDriveFragment).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
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
        mCreateDriveFragment.hideCreateDialogCompletely();
        switch (type) {
            case User.TYPE_DRIVER:
                if(mBounds == null) {
                    mBounds = new Bounds(0.0,0.0,0.0,0.0, 0, 0);
                }
                mMainViewModel.createDrive(time, startLocation, endLocation, seats, mBounds)
                        .observe(this, drive -> {
                            handleOnGoingDrive(drive);
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

    private void handleOnGoingDrive(Drive drive) {
        if (drive != null) {
            Intent UpdateDriveIntent = new Intent(MainActivity.this, ForegroundServices.class);
            UpdateDriveIntent.setAction(Constants.ACTION.STARTFOREGROUND_UPDATE_DRIVER_POSITION);
            UpdateDriveIntent.putExtra(ForegroundServices.INTENT_EXTRA_DRIVE_ID, drive.getId());
            startService(UpdateDriveIntent);
            updatePassengersMarkerPosition(drive.getId());
            mCreateDriveFragment.setDefaultValuesToDialog();
            mCancelDriveFab.setVisibility(View.VISIBLE);
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
    public void onNoMatchButtonClicked(int type) {
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
        mGoogleMap.setPadding(0, 0, 0, fragmentHeight);
        if (mMarkerCount == 1) {
            animateToLocation(mStartLocationMarker.getPosition());
        } else {
            zoomToFitRoute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGeofenceReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mForegroundReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter geofenceFilter = new IntentFilter(GEOFENCE_EVENTS_INTENT_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(mGeofenceReceiver, geofenceFilter);
        IntentFilter foregroundServiceFilter = new IntentFilter(FOREGROUND_SERVICE_INTENT_FILTER);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mForegroundReceiver, foregroundServiceFilter);
    }

    private void setUpGeofencing() {
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
    }

    private void createGeofence(PassengerRide passengerRide) {
        addGeofenceToList(passengerRide);
        addGeofences();
    }

    private void addGeofenceToList(PassengerRide passengerRide) {
        // Add pick up geoFence
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(passengerRide.getId() + GEOFENCE_TYPE_PICK_UP)
                .setCircularRegion(
                        passengerRide.getPickUpPosition().getLatitude(),
                        passengerRide.getPickUpPosition().getLongitude(),
                        GEOFENCE_RADIUS
                )
                .setExpirationDuration(GEOFENCE_TIME_OUT)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());

        // Add drop off geoFence
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(passengerRide.getId() + GEOFENCE_TYPE_DROP_OFF)
                .setCircularRegion(
                        passengerRide.getDropOffPosition().getLatitude(),
                        passengerRide.getDropOffPosition().getLongitude(),
                        GEOFENCE_RADIUS
                )
                .setExpirationDuration(GEOFENCE_TIME_OUT)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


    @SuppressLint("MissingPermission")
    private void addGeofences() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    private void removeGeofence(String requestId) {
        int geoFenceIdToRemove = 0;
        String geoFenceRequestIdToRemove = null;
        for (int i = 0; i < mGeofenceList.size(); i++) {
            if (mGeofenceList.get(i).getRequestId().equals(requestId)) {
                geoFenceIdToRemove = i;
                geoFenceRequestIdToRemove = mGeofenceList.get(i).getRequestId();
            }
        }
        mGeofenceList.remove(geoFenceIdToRemove);

        // Remove pending intent
        if (mGeofencingClient != null) {
            List<String> geoFencePendingIntentToRemove = new ArrayList<>();
            geoFencePendingIntentToRemove.add(geoFenceRequestIdToRemove);
            mGeofencingClient.removeGeofences(geoFencePendingIntentToRemove)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Timber.i("Geofence is removed");
                        } else {
                            Timber.e("Couldn't remove geofence: %s", task.getException());
                        }
                    });
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Timber.i("Geofences is added");
        } else {
            Timber.e("Couldn't add geofences: %s", task.getException());
        }
    }

    private void showDriverPickUpFragment(PassengerRide passengerRide) {
        mFragmentManager.beginTransaction().add(R.id.main_activity_dialog_container,
                DriverPassengerPickUpFragment.newInstance(passengerRide),
                DriverPassengerPickUpFragment.DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG).commit();
    }

    private void showPassengerPickUpFragment(Drive matchedDrive) {
        mFragmentManager.beginTransaction().add(R.id.main_activity_dialog_container,
                DriverPassengerPickUpFragment.newInstance(matchedDrive),
                DriverPassengerPickUpFragment.DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG).commit();
    }

    private void showDriverDropOffFragment(PassengerRide passengerRide) {
        mFragmentManager.beginTransaction().add(R.id.main_activity_dialog_container,
                DriverDropOffFragment.newInstance(passengerRide),
                DriverDropOffFragment.DRIVER_DROP_OFF_FRAGMENT_TAG).commit();
    }

    @Nullable
    private PassengerRide getPassengerRideFromLocalList(String passengerRideId) {
        for (int i = 0; i < mPassengerRides.size(); i++) {
            if (passengerRideId.equals(mPassengerRides.get(i).getId())) {
                return mPassengerRides.get(i);
            }
        }
        return null;
    }

    @Override
    public void onPickUpConfirmed(PassengerRide passengerRide) {
        removeFragment(mFragmentManager
                .findFragmentByTag(DriverPassengerPickUpFragment
                        .DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG));
        mMainViewModel.confirmPickUp(passengerRide);
    }

    @Override
    public void onPickUpNoShow(PassengerRide passengerRide) {

    }

    @Override
    public void onPickUpConfirmed(Drive drive) {
        removeFragment(mFragmentManager
                .findFragmentByTag(DriverPassengerPickUpFragment
                        .DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG));
        mMainViewModel.confirmPickUp(drive.getId());
    }

    @Override
    public void onPickUpNoShow(Drive drive) {

    }

    @Override
    public void onDropOffConfirmation(PassengerRide passengerRide) {
        removeFragment(mFragmentManager
                .findFragmentByTag(DriverDropOffFragment.DRIVER_DROP_OFF_FRAGMENT_TAG));
        mMainViewModel.confirmDropOff(passengerRide);
    }

    @Override
    public void onDropOffCanceled(PassengerRide passengerRide) {

    }

    private Animation getCloseDetailedInfoIfNeeded(View clickedFab) {
        String passengerId = (String)clickedFab.getTag();
        if (!passengerId.equals(mPassengerDetailedInformation.getTag())) {
            for (int i = 0; i < mPassengerContainer.getChildCount(); i++) {
                FloatingActionButton fab =(mPassengerContainer.getChildAt(i)
                        .findViewById(R.id.passenger_fab));
                if (fab.getTag().equals(mPassengerDetailedInformation.getTag())) {
                    return getCloseAnimation(fab, mPassengerDetailedInformation, 200);
                }
            }
        }
        return null;
    }


    private void updatePassengerDetailedInformation(FloatingActionButton fab) {
        String tag = (String)fab.getTag();
        if (tag == null) {
            return;
        }
        PassengerRide passengerRide = mPassengers.get(tag);
        if (passengerRide == null) {
            return;
        }
        ((TextView)mPassengerDetailedInformation.findViewById(R.id.passenger_name)).setText(
                passengerRide.getPassenger().getFullName());
        //TODO add rating to PassengerRide
        //TODO add image..
        ((TextView)mPassengerDetailedInformation.findViewById(R.id.passenger_start_location))
                .setText(passengerRide.getStartAddress());
        ((TextView)mPassengerDetailedInformation.findViewById(R.id.passenger_end_location))
                .setText(passengerRide.getEndAddress());
        //leaf value TODO add leaf value to passengerRide
        //price TODO add price to passengerRide
    }

    private void handleRideAborted(String rideId) {
        Toast.makeText(this, "Implement handling of this", Toast.LENGTH_LONG).show();
        //TODO Ride aborted from the driver, implement this
    }

    private void handleRemoveDrive() {
        String driveId = mActiveDriveIdList.get(0);
        if (driveId == null) {
            return;
        }

        mMainViewModel.removeCurrentDrive(driveId, task -> handleDriveRemoved(driveId));
    }

    private void handleDriveRemoved(String driveId) {
        mCancelDriveFab.setVisibility(View.INVISIBLE);
        mConfirmCancelDrive.setVisibility(View.INVISIBLE);
        addCreateDriveFragment();
        mCreateDriveFragment.showCreateDialog();
        mActiveDriveIdList.remove(driveId);
        mPassengerContainer.removeAllViews();
        mPassengerDetailedInformation = findViewById(R.id.passenger_detailed_information);
        mPassengerDetailedInformation.setVisibility(View.INVISIBLE);
    }

    private void toogleConfirmButton() {
        Animation animation;
        boolean isOpen = mConfirmCancelDrive.getVisibility() == View.VISIBLE;
        animation = isOpen ? getCloseAnimation(mCancelDriveFab, mConfirmCancelDrive, 200) :
                getOpenAnimation(mCancelDriveFab, mConfirmCancelDrive, 400);
        animation.setAnimationListener(new ViewAnimationListener(mConfirmCancelDrive, !isOpen));
        mConfirmCancelDrive.startAnimation(animation);
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.abort_passenger_button) {
            handleRideAborted((String) mPassengerDetailedInformation.getTag());
        } else if (view.getId() == R.id.cancel_drive) {
            toogleConfirmButton();
        } else if (view.getId() == R.id.confirm_cancel_drive_button) {
            handleRemoveDrive();
        } else if (view instanceof FloatingActionButton) {
            handlePassengerFabClicked(view);
        }
    }

    private void handlePassengerFabClicked(View view) {
        updatePassengerDetailedInformation((FloatingActionButton)view);
        AnimationSet as = new AnimationSet(false);
        Animation closeOtherAnimation = getCloseDetailedInfoIfNeeded(view);
        if (closeOtherAnimation != null) {
            closeOtherAnimation.setAnimationListener(new ViewAnimationListener(
                    mPassengerDetailedInformation, false));
            mPassengerDetailedInformation.setTag("");
            mPassengerDetailedInformation.startAnimation(closeOtherAnimation);
        }

        Object tag = mPassengerDetailedInformation.getTag();
        if (tag == null || tag.equals("")) {
            Animation openAnimation = getOpenAnimation(view, mPassengerDetailedInformation, 400);
            openAnimation.setAnimationListener(new ViewAnimationListener(
                    mPassengerDetailedInformation, true));

            as.addAnimation(openAnimation);
            mPassengerDetailedInformation.setTag(view.getTag());
            mPassengerDetailedInformation.startAnimation(openAnimation);
        } else {
            Animation closeAnimation = getCloseAnimation(view, mPassengerDetailedInformation, 200);
            mPassengerDetailedInformation.setTag("");
            closeAnimation.setAnimationListener(new ViewAnimationListener(
                    mPassengerDetailedInformation, false));
            mPassengerDetailedInformation.startAnimation(closeAnimation);
        }
    }

    private class ViewAnimationListener implements Animation.AnimationListener {
        private final boolean mIsOpenAnimation;
        private final View mView;
        ViewAnimationListener(@NonNull View view, boolean isOpenAnimation) {
            mIsOpenAnimation = isOpenAnimation;
            mView = view;
        }
        @Override
        public void onAnimationStart(Animation animation) {
            if (mIsOpenAnimation) {
                mView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!mIsOpenAnimation) {
                mView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    /**
     * Creates an animation of the viewToAnimate view. It will be animated from it's normal position
     * to the position of the fromView. The animation will also shrink the view.
     * @param fromView
     * @param viewToAnimate
     * @param duration
     * @return
     */
    private static AnimationSet getCloseAnimation(View fromView, View viewToAnimate, int duration) {
        int[] viewToAnimateLocation = new int[2];
        int[] fromViewLocation = new int[2];
        viewToAnimate.getLocationOnScreen(viewToAnimateLocation);
        fromView.getLocationOnScreen(fromViewLocation);
        float startX = fromViewLocation[0] + (fromView.getWidth()/2) - viewToAnimateLocation[0];
        float startY = fromViewLocation[1] + (fromView.getHeight()/2) - viewToAnimateLocation[1];
        AnimationSet animSet = new AnimationSet(false);
        Animation scaleDown = AnimationUtils.loadAnimation(fromView.getContext(),
                R.anim.scale_down_detailed_info);

        animSet.addAnimation(scaleDown);
        animSet.addAnimation(new TranslateAnimation(0, startX, 0, startY));
        animSet.setDuration(duration);
        return animSet;
    }

    /**
     * Creates an animation of the viewToAnimate view. It will be animated from the position of the
     * fromView to it's normal position. The animation will expand the view from 0% to 100% of it's
     * normal size.
     * @param fromView
     * @param viewToAnimate
     * @param duration
     * @return
     */
    private static AnimationSet getOpenAnimation(View fromView, View viewToAnimate, int duration) {
        int[] detailedViewLocation = new int[2];
        int[] fabLocation = new int[2];
        viewToAnimate.getLocationOnScreen(detailedViewLocation);
        fromView.getLocationOnScreen(fabLocation);

        float startX = fabLocation[0] + (fromView.getWidth()/2) - detailedViewLocation[0];
        float startY = fabLocation[1] + (fromView.getHeight()/2) - detailedViewLocation[1];
        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(new AlphaAnimation(0, 1));
        Animation scaleUpAnimation = AnimationUtils.loadAnimation(fromView.getContext(),
                R.anim.scale_up_detailed_info);

        animSet.addAnimation(scaleUpAnimation);
        animSet.addAnimation(new TranslateAnimation(startX, 0, startY, 0));
        animSet.setDuration(duration);
        return animSet;
    }

    public void removeFragment(Fragment fragment){
        mFragmentManager.beginTransaction().remove(fragment).commit();
    }

    private class GeofenceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                Timber.i("onReceive RequestId: %s ", intent.getExtras()
                        .get(GEOFENCE_EVENTS_REQUEST_ID));

                String geofenceRequestId = intent.getExtras()
                        .getString(GEOFENCE_EVENTS_REQUEST_ID);
                String passengerRideId;
                String geoFenceType;

                if (geofenceRequestId != null) {
                    passengerRideId
                            = geofenceRequestId.substring(0, geofenceRequestId.length() - 1);
                    geoFenceType
                            = geofenceRequestId.substring(geofenceRequestId.length() - 1);
                    if (geoFenceType.equals(GEOFENCE_TYPE_PICK_UP)) {
                        if (mCurrentLoggedInUser.getType() == User.TYPE_DRIVER) {
                            showDriverPickUpFragment(getPassengerRideFromLocalList(passengerRideId));
                        }
                    } else if (geoFenceType.equals(GEOFENCE_TYPE_DROP_OFF)) {
                        if (mCurrentLoggedInUser.getType() == User.TYPE_DRIVER) {
                            showDriverDropOffFragment(getPassengerRideFromLocalList(passengerRideId));
                        }
                        removeGeofence(geofenceRequestId);
                    }
                }
            }
        }
    }

    private class ForegroundServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                if (intent.getExtras().getInt(DIALOG_TO_SHOW) == TYPE_PICK_UP) {
                    removePassengerNotificationDialog();
                    showPassengerPickUpFragment(mMainViewModel.getMatchedDrive());
                }
            }
        }
    }
}