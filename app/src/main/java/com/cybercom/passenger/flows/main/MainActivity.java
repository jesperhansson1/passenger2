package com.cybercom.passenger.flows.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
import android.net.Uri;
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
import android.widget.ImageView;
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
import com.cybercom.passenger.flows.passengernotification.DriveInformationDialog;
import com.cybercom.passenger.flows.payment.CalculatePrice;
import com.cybercom.passenger.flows.payment.StripeAsyncTask;
import com.cybercom.passenger.flows.pickupfragment.DriverPassengerPickUpFragment;
import com.cybercom.passenger.flows.progressfindingcar.FindingCarProgressDialog;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.PassengerRide;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.Route;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.route.FetchRoute;
import com.cybercom.passenger.service.Constants;
import com.cybercom.passenger.service.ForegroundServices;
import com.cybercom.passenger.service.GeofenceTransitionsIntentService;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.NotificationHelper;
import com.cybercom.passenger.utils.RoundCornersTransformation;
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
import com.google.android.gms.maps.model.CameraPosition;
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
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CARD_ERROR;
import static com.cybercom.passenger.flows.payment.PaymentConstants.GOOGLE_API_ERROR;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RESERVE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.SPLIT_CHAR;
import static com.cybercom.passenger.flows.payment.PaymentHelper.createChargeHashMap;
import static com.cybercom.passenger.model.User.TYPE_DRIVER;
import static com.cybercom.passenger.model.User.TYPE_PASSENGER;
import static com.cybercom.passenger.utils.RoundCornersTransformation.RADIUS;

public class MainActivity extends AppCompatActivity implements
        CreateDriveFragment.CreateRideFragmentListener,
        AcceptRejectPassengerDialog.ConfirmationListener,
        DriveInformationDialog.PassengerNotificationListener,
        OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener,
        CreateDriveFragment.OnPlaceMarkerIconClickListener, FetchRoute.OnRouteCompletion,
        CreateDriveFragment.OnFinishedCreatingDriveOrDriveRequest,
        FindingCarProgressDialog.FindingCarListener, GoogleMap.OnMyLocationButtonClickListener,
        NoMatchFragment.NoMatchButtonListener, FragmentSizeListener, OnCompleteListener<Void>,
        DriverPassengerPickUpFragment.DriverPassengerPickUpButtonClickListener,
        DriverDropOffFragment.DriverDropOffConfirmationListener, View.OnClickListener,
        StripeAsyncTask.StripeAsyncTaskDelegate{


    private static final int DELAY_BEFORE_SHOWING_CREATE_DRIVE_AFTER_LOCATION_CHANGED = 1500;
    private static final int PLACE_MARKER_INFO_FADE_DURATION = 1000;
    private static final float PLACE_MARKER_INFO_FADE_OUT_TO = 0.0f;
    private static final float PLACE_MARKER_INFO_FADE_IN_TO = 1.0f;
    private static final float CHANGE_CAMERA_BEARING_SPEED_THRESHOLD = 1.0f;
    public static final int ZOOM_LEVEL_FOLLOW_DRIVER = 18;
    private static final int ZOOM_LEVEL_MY_LOCATION = 17;
    private static final float ZOOM_LEVEL_STREETS = 15;
    private static final String DRIVE_ID = "driveId";
    private static final String PASSENGER_RIDE_KEY = "passengerRideKey";
    public static final int GEOFENCE_RADIUS = 100;
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
    private static final int DEVIATE_FROM_ROUTE_REROUTE_DISTANCE_M = 150;
    private static final int TEN_SEC_DELAY_FOR_DEVIATION_CALC = 10000;

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

    private Date mLatestDriveDeviationCheck = new Date();

    // TODO remove this or the other one..
    // Driver client member
    private List<PassengerRide> mPassengerRides = new ArrayList<>();

    private GeofenceBroadcastReceiver mGeofenceReceiver;
    private ForegroundServiceReceiver mForegroundReceiver;

    private User mCurrentLoggedInUser;

    private HashMap<String, PassengerRide> mPassengers = new HashMap<>();

    private Bounds mBounds;
    private List<String> mActiveDriveIdList = new ArrayList<>();
    private Observer<Location> mLocationObserverForCameraUpdates;
    private MutableLiveData<Location> mDriverCurrentLocationLiveData;

    // A Drive is active when mActiveDrive != null (Only happens when user is Driver)
    private Drive mActiveDrive;

    // A PassengerRide is active mActivePassengerRide != null (Only happens when user is Passenger)
    private PassengerRide mActivePassengerRide;
    private Fragment mDriveInformationDialog;
    private String mGoogleApiKey;
    private boolean mRefund = false; //flag for refund
    private String mRefundChargeId; //chargeid for which refund has to be done

   /* public double mPrice = 0.0;
    public long mTime = 0;
    public Position mStartLocation = null;
    public Position mEndLocation = null;
    public int mSeats = 0;*/

    DriveRequest mPendingDriveRequest;

    private boolean mActiveDriveZoomFlag = true;



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

        initUI();
        if (mUser != null) {
            mMainViewModel.refreshToken(FirebaseInstanceId.getInstance().getToken());
            mMainViewModel.getUser().observe(this, user -> {
                Timber.i("User: %s logged in", user);
                mCurrentLoggedInUser = user;
                if(mCurrentLoggedInUser.getCustomerId() != null)
                    Timber.d(" stripe id is " + mCurrentLoggedInUser.getCustomerId() +
                            " type is " + mCurrentLoggedInUser.getType());
                initObservers();
                setUpGeofencing();
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

        mGoogleApiKey = getResources().getString(R.string.google_api_key);
    }

    private void createNotificationChannels() {
        new NotificationHelper(this);
    }

    private void createPassengerRide(Drive drive, Position startPosition, Position endPosition,
                                     String startAddress, String endAddress, String chargeId,
                                     double price) {
        mMainViewModel.createPassengerRide(drive, startPosition, endPosition, startAddress,
                endAddress, chargeId, price).observe(this, passengerRide -> {
                    Timber.d("PassengerRide successfully created: %s", passengerRide);
        });
    }

    // Passenger client method
    private void handleOnGoingPassengerRide(PassengerRide passengerRide) {
        Timber.d("handleOnGoingPassengerRide %s", passengerRide);
        if (mActivePassengerRide != null) {
            // There could only be one PassengerRide
            Timber.e("More than one PassengerRide...");
            return;
        }
        mActivePassengerRide = passengerRide;
        mCreateDriveFragment.hideCreateDialogCompletely();
        showDriveInformationDialog(passengerRide.getDrive(),
                mActivePassengerRide.isPickUpConfirmed(), mActivePassengerRide.getPrice());
        updateDriversMarkerPosition(passengerRide.getDrive().getId());
        dismissMatchingInProgressDialog();
        startPassengerForegroundService(passengerRide);

        // Observe the active PassengerRide (Note: the database model version..) for changes
        mMainViewModel.getPassengerRideById(mActivePassengerRide.getId()).observe(this,
                (com.cybercom.passenger.repository.databasemodel.PassengerRide passengerRideDatabaseModel) -> {
                    if (passengerRideDatabaseModel != null && passengerRideDatabaseModel.getPassengerId() != null) {
                        if (!mActivePassengerRide.isCancelled() &&
                                passengerRideDatabaseModel.isCancelled()) {
                            handlePassengerRideRemoved(mActivePassengerRide.getId());
                            return;
                        }
                        if (!mActivePassengerRide.isPickUpConfirmed() &&
                                passengerRideDatabaseModel.isPickUpConfirmed()) {
                            removeFragment(mFragmentManager
                                    .findFragmentByTag(DriverPassengerPickUpFragment
                                            .DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG));
                            showDriveInformationDialog(mActivePassengerRide.getDrive(),
                                    passengerRideDatabaseModel.isPickUpConfirmed(),
                                    mActivePassengerRide.getPrice());
                            mActivePassengerRide.setPickUpConfirmed(true);
                        }
                        if (!mActivePassengerRide.isDropOffConfirmed() &&
                                passengerRideDatabaseModel.isDropOffConfirmed()) {
                            mActivePassengerRide.setDropOffConfirmed(true);
                            handlePassengerRideRemoved(mActivePassengerRide.getId());
                        }
                    } else {
                        if (mActivePassengerRide != null) {
                            handlePassengerRideRemoved(mActivePassengerRide.getId());
                        }
                    }
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
        // Both drivers and passengers could receive notifications
        observeOnNotifications();

        // Drivers could have active drives
        if (mCurrentLoggedInUser.getType() == TYPE_DRIVER) {
            observeOnActiveDrive();
        }

        // A passenger could have a PassengerRide
        if (mCurrentLoggedInUser.getType() == User.TYPE_PASSENGER) {
            observeOnActivePassengerRide();
        }
    }

    // Passenger client method
    private void observeOnActivePassengerRide() {
        mMainViewModel.getActivePassengerRide().observe(this, passengerRide -> {
            if (passengerRide != null && !passengerRide.isDropOffConfirmed() &&
                    !passengerRide.isCancelled()) {
                handleOnGoingPassengerRide(passengerRide);
            }
        });
    }


    private void observeOnActiveDrive() {
        final LifecycleOwner lifecycleOwner = this;

        mMainViewModel.getActiveDrive().observe(this, drive -> {
            if (drive != null) {
                if (mActiveDriveIdList.contains(drive.getId())) {
                    // Make sure to only observe once
                    Timber.d("Have already added PassengerRide observer");
                    return;
                }

                if (mActiveDriveIdList.size() > 1) {
                    Toast.makeText(this, "Error: More than one drive active " +
                            "for the current user", Toast.LENGTH_LONG).show();
                }

                mActiveDriveIdList.add(drive.getId());
                mActiveDrive = drive;
                mActiveDriveZoomFlag = true;
                mMainViewModel.getPassengerRides(drive.getId()).observe(this, passengerRide -> {
                    if (passengerRide != null && !passengerRide.isCancelled()
                            && !passengerRide.isDropOffConfirmed()) {
                        if (isPassengerRideAlreadyAddedToLocalList(passengerRide)) {
                            replacePassengerRide(passengerRide);
                        } else {
                            mPassengerRides.add(passengerRide);
                            createGeofence(passengerRide);
                        }
                    }
                    handlePassengerChanged(passengerRide);
                    updateCancelDriveButtonsVisibility();
                });

                handleOnGoingDrive(drive);
            }
        });
    }

    private void updateCancelDriveButtonsVisibility() {
        mConfirmCancelDrive.setVisibility(View.INVISIBLE);
        if (mActiveDrive != null) {
            if (mPassengers.size() > 0) {
                mCancelDriveFab.setVisibility(View.INVISIBLE);
            } else {
                mCancelDriveFab.setVisibility(View.VISIBLE);
            }
        } else {
            mCancelDriveFab.setVisibility(View.INVISIBLE);
        }
    }

    private void observeOnNotifications() {
        mMainViewModel.getIncomingNotifications().observe(this, notification -> {
            if (notification == null) {
                return;
            }
            Timber.d("Notification to be displayed: %s", notification.toString());

            switch (notification.getType()) {
                // Driver client can receive these
                case Notification.REQUEST_DRIVE:
                    showDriverConfirmationDialogFragment(notification);
                    break;
                // Passenger clients can receive these
                case Notification.ACCEPT_PASSENGER:
                    String startAddress = mMainViewModel.getAddressFromLocation(
                            LocationHelper.convertPositionToLocation(notification.getDriveRequest()
                                    .getStartLocation()));
                    String endAddress = mMainViewModel.getAddressFromLocation(
                            LocationHelper.convertPositionToLocation(
                                    notification.getDriveRequest().getEndLocation()));

                    createPassengerRide(notification.getDrive(),
                            notification.getDriveRequest().getStartLocation(),
                            notification.getDriveRequest().getEndLocation(), startAddress,
                            endAddress, notification.getDriveRequest().getChargeId(),
                            notification.getDriveRequest().getPrice());
                    mMainViewModel.getNextNotification();
                    break;
                    // Passenger clients can receive these
                case Notification.REJECT_PASSENGER:
                    matchDriveRequest(notification.getDriveRequest(),
                            mMainViewModel.getDriveRequestRadiusMultiplier());
                    mMainViewModel.getNextNotification();
                    break;
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


    private void moveCameraToLocation(Location location) {
        CameraPosition cameraPosition;
        CameraPosition.Builder cameraPositionBuilder;

        if (mActiveDriveZoomFlag) {
            cameraPositionBuilder = new CameraPosition.Builder()
                    .zoom(ZOOM_LEVEL_FOLLOW_DRIVER);
            cameraPosition = cameraPositionBuilder.target(new LatLng(location.getLatitude(),
                    location.getLongitude())).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mMainViewModel.setInitialZoomDone(true);
            mActiveDriveZoomFlag = false;
        } else {
            cameraPositionBuilder = new CameraPosition.Builder()
                    .zoom(mGoogleMap.getCameraPosition().zoom);
            // This is needed to prevent erratic camera updates when car has stopped.
            if (location.hasSpeed()
                    && location.getSpeed() > CHANGE_CAMERA_BEARING_SPEED_THRESHOLD) {

                cameraPositionBuilder.bearing(location.getBearing());
            }
            cameraPosition = cameraPositionBuilder.target(new LatLng(location.getLatitude(),
                    location.getLongitude())).build();

            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void checkIfDriverHasDeviatedFromRoute(Location location) {
        if (mRoute == null) {
            return;
        }
        Date currentDate = new Date(System.currentTimeMillis());
        if (mLatestDriveDeviationCheck.getTime() - currentDate.getTime()
                > TEN_SEC_DELAY_FOR_DEVIATION_CALC) {
            return;
        }

        mLatestDriveDeviationCheck = currentDate;

        List<LatLng> routePoints = mRoute.getPoints();
        boolean isCloseToRoute = false;

        for (LatLng routePoint : routePoints) {
            Location routeLocation = new Location("routePoint");
            routeLocation.setLatitude(routePoint.latitude);
            routeLocation.setLongitude(routePoint.longitude);
            if (DEVIATE_FROM_ROUTE_REROUTE_DISTANCE_M > location.distanceTo(routeLocation)) {
                isCloseToRoute = true;
                break;
            }
        }
        if (!isCloseToRoute) {
            Position driveStartLocation = new Position(null, location.getLatitude(),
                        location.getLongitude());
            mActiveDriveZoomFlag = true;
            rerouteDrive(mActiveDrive, null, driveStartLocation);
        }
    }

    /**
     * Start moving the camera to center the current position (the driver's) and also turn the
     * camera so that the current direction where the car is heading is always upwards.
     */
    private void observerDriverPositionUpdates() {
        mActiveDriveZoomFlag = true;

        mLocationObserverForCameraUpdates = location -> {
            moveCameraToLocation(location);
            checkIfDriverHasDeviatedFromRoute(location);
        };

        mDriverCurrentLocationLiveData = mMainViewModel.getDriverCurrentLocation();
        mDriverCurrentLocationLiveData.observe(this, mLocationObserverForCameraUpdates);
    }

    private void stopCameraUpdatesOnPositionUpdates() {
        if (mDriverCurrentLocationLiveData != null) {
            mDriverCurrentLocationLiveData.removeObserver(mLocationObserverForCameraUpdates);
        }
    }

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
        mFindMatch = mMainViewModel.findBestDriveMatch(driveRequest, radiusMultiplier,
                mGoogleApiKey);
        showMatchingInProgressDialog(driveRequest.getPrice(), driveRequest.getChargeId());
        mTimer = mMainViewModel.setFindMatchTimer();

        mMatchObserver = drive -> {
            if (drive != null) {
                Timber.i("matched drive %s", drive);
                mMainViewModel.addRequestDriveNotification(driveRequest, drive);
                cancelMatchingDrive();
            } else {
                Timber.i("No drives match for the moment. Keep listening.");
            }
        };

        mFindMatch.observe(lifecycleOwner, mMatchObserver);

        mTimer.observe(lifecycleOwner, aBoolean -> {
            showNoMatchDialog();
            cancelMatchingDrive();
            dismissMatchingInProgressDialog();

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
        mMainViewModel.cancelDriveMatch();

    }

    private void showMatchingInProgressDialog(double amount, String chargeId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(
                FindingCarProgressDialog.MATCHING_IN_PROGRESS);

        if (fragment != null) {
            fragment.dismiss();
        }

        FindingCarProgressDialog findingCarProgressDialog = FindingCarProgressDialog.getInstance();
        findingCarProgressDialog.setAmount(amount);
        findingCarProgressDialog.setChargeId(chargeId);
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

        mPlaceMarkerInformation = findViewById(R.id.main_activity_place_marker_info);
        mPassengerContainer = findViewById(R.id.passenger_container);
        mPassengerDetailedInformation = findViewById(R.id.passenger_detailed_information);
        mPassengerDetailedInformation.findViewById(R.id.abort_passenger_button)
                .setOnClickListener(this);
        mPassengerDetailedInformation.findViewById(R.id.dropoff_passenger_button)
                .setOnClickListener(this);
        mCancelDriveFab = findViewById(R.id.cancel_drive);
        mCancelDriveFab.setOnClickListener(this);
        mConfirmCancelDrive = findViewById(R.id.confirm_cancel_drive_button);
        mConfirmCancelDrive.setOnClickListener(this);
    }

    private void setStartMarkerLocation() {
        mMainViewModel.getLastKnownLocation(location -> {
            if (location != null) {
                mMainViewModel.setStartMarkerLocation(location);
                placeStartLocationMarker();
                mFragmentManager.beginTransaction()
                        .add(R.id.main_activity_dialog_container, mCreateDriveFragment).commit();
                mIsFragmentAdded = true;
            }
        });
    }

    // Driver client method. Handle changes in a PassengerRide.
    private void handlePassengerChanged(PassengerRide passengerRide) {
        if (passengerRide == null) {
            return;
        }

        if (mPassengers.get(passengerRide.getId()) == null
            && !passengerRide.isDropOffConfirmed() && !passengerRide.isCancelled()) {
            mPassengers.put(passengerRide.getId(), passengerRide);
            addPassengerFab(passengerRide.getId(), passengerRide.getPassenger().getUserId());
        } else if (passengerRide.isDropOffConfirmed() || passengerRide.isCancelled()) {
            mPassengers.remove(passengerRide.getId());
            removePassengerFab(passengerRide.getId());
        } else {
            if (!mPassengers.get(passengerRide.getId()).isPickUpConfirmed() &&
                    passengerRide.isPickUpConfirmed()) {
                // Passenger has just been picked up
                removeFragment(mFragmentManager
                        .findFragmentByTag(DriverPassengerPickUpFragment
                                .DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG));
                removeGeofence(passengerRide.getId() + GEOFENCE_TYPE_PICK_UP);
            }
            mPassengers.put(passengerRide.getId(), passengerRide);
            updatePassengerDetailedInformation(passengerRide);
        }
        rerouteDrive(passengerRide.getDrive(), passengerRide,
                passengerRide.getDrive().getCurrentPosition());
    }

    private void rerouteDrive(@NonNull Drive drive, @Nullable PassengerRide passengerRide,
                              @Nullable Position currentDriverLocation) {

        Position driveStartLocation = drive.getStartLocation();
        Position driveEndLocation = drive.getEndLocation();
        if (currentDriverLocation != null) {
            driveStartLocation = currentDriverLocation;
        }
        List<LatLng> latLngPointsList = new ArrayList();

        if (passengerRide != null) {
            latLngPointsList.addAll(getLatLngsFromPassengerRide(passengerRide));
        }

        for (PassengerRide onBoardedPassenger : mPassengers.values()) {
            latLngPointsList.addAll(getLatLngsFromPassengerRide(onBoardedPassenger));
        }

        reRoute(createLatLngFromPosition(driveStartLocation),
                createLatLngFromPosition(driveEndLocation), latLngPointsList);
    }

    private List<LatLng> getLatLngsFromPassengerRide(@NonNull PassengerRide passengerRide) {
        List<LatLng> latLngs = new ArrayList<>();
        if (!passengerRide.isPickUpConfirmed()) {
            latLngs.add(createLatLngFromPosition(passengerRide.getPickUpPosition()));
        }
        if (!passengerRide.isDropOffConfirmed()) {
            latLngs.add(createLatLngFromPosition(passengerRide.getDropOffPosition()));
        }
        return latLngs;
    }

    private void handlePassengerCancelled(String passengerRideId) {
        mPassengers.remove(passengerRideId);
        if (mPassengers.isEmpty()) {
            updateCancelDriveButtonsVisibility();
        }
        removePassengerFab(passengerRideId);
        removeGeofence(passengerRideId + GEOFENCE_TYPE_PICK_UP);
        removeGeofence(passengerRideId + GEOFENCE_TYPE_DROP_OFF);

        findViewById(R.id.passenger_detailed_information).setVisibility(View.INVISIBLE);
        // TODO: Recalculate the route for the drive
    }


    @SuppressLint("TimberArgCount")
    private void addPassengerFab(@NonNull String rideId, String userId) {

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

            mMainViewModel.getImageUri(userId).observe(this, uri -> {
                //Todo: resize and circular image
                Timber.d("image uri ", uri.toString());
                try {
                    URL url = new URL(uri.toString());
                    Timber.d("image url ", url);
                    Picasso.with(getApplicationContext()).load(String.valueOf(url)).fit()
                            .centerCrop().transform(new RoundCornersTransformation(fab.getWidth(),
                            0, true, true)).into(fab);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
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
        LinearLayout child;
        for (int i = 0; i < mPassengerContainer.getChildCount(); i++) {
            child = (LinearLayout)mPassengerContainer.getChildAt(i);
            if (child.getChildAt(0) != null && rideId.equals(child.getChildAt(0).getTag())) {
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

        setStartMarkerLocation();

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

                LatLng origin = new LatLng(
                        mMainViewModel.getStartMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getStartMarkerLocation().getValue().getLongitude());

                LatLng destination = new LatLng(
                        mMainViewModel.getEndMarkerLocation().getValue().getLatitude(),
                        mMainViewModel.getEndMarkerLocation().getValue().getLongitude());

                new FetchRoute(origin, destination, null, this, mGoogleApiKey);
            }
        }
    }

    private void reRoute(LatLng origin, LatLng destination, List<LatLng> latLngs) {
        new FetchRoute(origin, destination, latLngs, this, mGoogleApiKey);
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

    private void showDriveInformationDialog(Drive drive, boolean pickUpConfirmed, double price) {
            removeDriveInformationDialog();

        mDriveInformationDialog = DriveInformationDialog.getInstance(
                drive, pickUpConfirmed, price);

        mFragmentManager.beginTransaction().add(R.id.main_activity_dialog_container,
                mDriveInformationDialog).commit();
    }

    private void removeDriveInformationDialog() {
        if (mDriveInformationDialog != null) {
            mFragmentManager.beginTransaction().remove(mDriveInformationDialog).commit();
        }
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

    public static LatLng createLatLngFromPosition(@NonNull Position position) {
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
        mMainViewModel.getNextNotification();
    }

    @Override
    public void onCancelPassengerRide() {
        // Passenger has clicked on Cancel before getting picked up
        mMainViewModel.getNextNotification();

        if (mActivePassengerRide != null) {
            mMainViewModel.cancelPassengerRide(mActivePassengerRide.getId());
        }
    }

    @Override
    public void onDropOffPassengerRide() {
        // Passenger has clicked drop off before the end destination has been reached
        mMainViewModel.getNextNotification();

        if (mActivePassengerRide != null) {
            mMainViewModel.confirmDropOff(mActivePassengerRide.getId());
        }
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

        if (mRoute != null) {
            mRoute.remove();
        }
        mRoute = mGoogleMap.addPolyline(polylineOptions);

        // Only zoom to fit when no drive or ride is active
        if (mActiveDrive == null && mActivePassengerRide == null) {
            zoomToFitRoute();
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
        removeCreateDriveFragment();
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
                    setStartMarkerLocation();

                } else {

                    // permission denied
                    // Disable the functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    public void onCreateRide(long time, int type, Position startLocation, Position endLocation,
                             int seats) {
        switch (type) {
            case TYPE_DRIVER:
                if(mBounds == null) {
                    mBounds = new Bounds(0.0,0.0,0.0,0.0, 0, 0);
                }
                mMainViewModel.createDrive(time, startLocation, endLocation, seats, mBounds)
                        .observe(this, drive -> {
                            Timber.d("Drive successfully created: %s", drive);
                        });
                break;
            case User.TYPE_PASSENGER:
                mPendingDriveRequest = new DriveRequest(null,null,time,startLocation,endLocation,seats,0.0,null,null, 0.0);
                getPrice(seats);
                break;
        }
    }

    // Driver client method
    private void handleOnGoingDrive(Drive drive) {
        if (drive != null) {
            startDriverForegroundService(drive);
            observerDriverPositionUpdates();
            updatePassengersMarkerPosition(drive.getId());
            updateCancelDriveButtonsVisibility();
            mCreateDriveFragment.hideCreateDialogCompletely();
            mCreateDriveFragment.setDefaultValuesToDialog();
        }
    }

    private void startDriverForegroundService(Drive drive) {
        Intent UpdateDriveIntent = new Intent(MainActivity.this, ForegroundServices.class);
        UpdateDriveIntent.setAction(Constants.ACTION.STARTFOREGROUND_DRIVER_CLIENT);
        UpdateDriveIntent.putExtra(ForegroundServices.INTENT_EXTRA_DRIVE_ID, drive.getId());
        startService(UpdateDriveIntent);
    }

    private void startPassengerForegroundService(PassengerRide passengerRide) {
        Intent updatePassengerIntent = new Intent(MainActivity.this, ForegroundServices.class);
        updatePassengerIntent.setAction(
                Constants.ACTION.STARTFOREGROUND_PASSENGER_CLIENT);
        updatePassengerIntent.putExtra(ForegroundServices.INTENT_EXTRA_PASSENGER_RIDE_ID,
                passengerRide.getId());
        updatePassengerIntent.putExtra(ForegroundServices.INTENT_EXTRA_DRIVE_ID,
                passengerRide.getDrive().getId());

        startService(updatePassengerIntent);
    }

    @Override
    public void onCancelFindingCarPressed(Boolean isCancelPressed, String chargeId) {
        mMainViewModel.refundFull(chargeId);
        cancelMatchingDrive();
        dismissMatchingInProgressDialog();
        if (!mIsFragmentAdded) {
            addCreateDriveFragment();
        }
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
        // When Drive is active the camera should follow the driver
        if (mActiveDrive != null) {
            return;
        }
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

        if (!passengerRide.isPickUpConfirmed()) {
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
        }

        if (passengerRide.isDropOffConfirmed()) {
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
        int geoFenceIdToRemove = -1;
        String geoFenceRequestIdToRemove = null;
        for (int i = 0; i < mGeofenceList.size(); i++) {
            if (mGeofenceList.get(i).getRequestId().equals(requestId)) {
                geoFenceIdToRemove = i;
                geoFenceRequestIdToRemove = mGeofenceList.get(i).getRequestId();
            }
        }
        if (geoFenceIdToRemove != -1) {
            mGeofenceList.remove(geoFenceIdToRemove);
        }

        // Remove pending intent
        if (mGeofencingClient != null) {
            List<String> geoFencePendingIntentToRemove = new ArrayList<>();
            geoFencePendingIntentToRemove.add(geoFenceRequestIdToRemove);
            mGeofencingClient.removeGeofences(geoFencePendingIntentToRemove)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Timber.i("Geofence %s is removed", requestId);
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

    private void showPassengerPickUpFragment(PassengerRide passengerRide) {
        mFragmentManager.beginTransaction().add(R.id.main_activity_dialog_container,
                DriverPassengerPickUpFragment.newInstance(passengerRide),
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
        // Driver has confirmed pick up
        removeFragment(mFragmentManager
                .findFragmentByTag(DriverPassengerPickUpFragment
                        .DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG));
        mMainViewModel.confirmPickUp(passengerRide);
    }

    @Override
    public void onPickUpNoShow(PassengerRide passengerRide) {
        removeFragment(mFragmentManager
                .findFragmentByTag(DriverPassengerPickUpFragment
                        .DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG));
        // Driver has reported no show
        if (mCurrentLoggedInUser.getType() == TYPE_DRIVER){
            Timber.d("Driver has reported no show. Minimum fee is charged for customer and remaining amount refunded");
            mMainViewModel.noShowPassenger(passengerRide.getChargeId(),
                    passengerRide.getDrive().getDriver().getCustomerId());
        }
        // Passenger has reported no show
        if (mCurrentLoggedInUser.getType() == TYPE_PASSENGER){
            Timber.d("Passenger has reported no show. Refund full amount to customer");
            addCreateDriveFragment();
            mMainViewModel.refundFull(passengerRide.getChargeId());
        }
    }

    @Override
    public void onDropOffConfirmation(PassengerRide passengerRide) {
        removeFragment(mFragmentManager
                .findFragmentByTag(DriverDropOffFragment.DRIVER_DROP_OFF_FRAGMENT_TAG));
        mMainViewModel.confirmDropOff(passengerRide.getId());
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
        updatePassengerDetailedInformation(passengerRide);
    }

    @SuppressLint("TimberArgCount")
    private void updatePassengerDetailedInformation(PassengerRide passengerRide) {
        if (passengerRide == null) {
            return;
        }

        ImageView passengerImageView = (ImageView)findViewById(R.id.passenger_thumbnail);

        LiveData<Uri> imageUri = mMainViewModel.getImageUri(passengerRide.getPassenger().getUserId());
        imageUri.observe(this,uri -> {
            Timber.d("image uri ", uri.toString());
            try {
                URL url = new URL(uri.toString());
                Timber.d("image url ", url);
                Picasso.with(getApplicationContext()).load(String.valueOf(url)).fit().centerCrop()
                        .transform(new RoundCornersTransformation(RADIUS, 0, true, false)).into(passengerImageView);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        ((TextView)mPassengerDetailedInformation.findViewById(R.id.passenger_name)).setText(
                passengerRide.getPassenger().getFullName());
        //TODO add rating to PassengerRide
        //TODO add image..
        ((TextView) mPassengerDetailedInformation.findViewById(R.id.passenger_start_location))
                .setText(passengerRide.getStartAddress());
        if (passengerRide.isPickUpConfirmed()) {
            mPassengerDetailedInformation.findViewById(R.id.dropoff_passenger_button)
                    .setVisibility(View.VISIBLE);
            mPassengerDetailedInformation.findViewById(R.id.abort_passenger_button)
                    .setVisibility(View.GONE);
        } else {
            mPassengerDetailedInformation.findViewById(R.id.dropoff_passenger_button)
                    .setVisibility(View.GONE);
            mPassengerDetailedInformation.findViewById(R.id.abort_passenger_button)
                    .setVisibility(View.VISIBLE);
        }
        ((TextView)mPassengerDetailedInformation.findViewById(R.id.passenger_end_location))
                .setText(passengerRide.getEndAddress());
        //leaf value TODO add leaf value to passengerRide
        //price TODO add price to passengerRide
    }

    // Driver client method
    private void handleRemoveDrive() {
        if (mPassengers.size() > 0) {
            Toast.makeText(this, R.string.main_activity_active_rides_error_message,
                    Toast.LENGTH_LONG).show();
            return;
        }

        String driveId = mActiveDriveIdList.get(0);
        if (driveId == null) {
            return;
        }

        mMainViewModel.removeCurrentDrive(driveId, task -> handleDriveRemoved(driveId));
    }

    // Driver client method called when a Drive has been removed
    private void handleDriveRemoved(String driveId) {
        addCreateDriveFragment();
        stopForegroundService();
        stopCameraUpdatesOnPositionUpdates();
        mCreateDriveFragment.showCreateDialog();
        mActiveDrive = null;
        mActiveDriveIdList.remove(driveId);
        mPassengerContainer.removeAllViews();
        mPassengerDetailedInformation = findViewById(R.id.passenger_detailed_information);
        mPassengerDetailedInformation.setVisibility(View.INVISIBLE);
        updateCancelDriveButtonsVisibility();
    }

    private void stopForegroundService() {
        Intent foregroundServiceIntent = new Intent(MainActivity.this, ForegroundServices.class);
        stopService(foregroundServiceIntent);
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
            // The driver has clicked cancel before being picked up
            mMainViewModel.cancelPassengerRide((String) mPassengerDetailedInformation.getTag());
            handlePassengerCancelled((String) mPassengerDetailedInformation.getTag());
        } else if (view.getId() == R.id.dropoff_passenger_button) {
            // The driver has clicked drop off before the end of the drive
            mMainViewModel.confirmDropOff((String) mPassengerDetailedInformation.getTag());
            handlePassengerCancelled((String) mPassengerDetailedInformation.getTag());
        } else if (view.getId() == R.id.cancel_drive) {
            // The driver has clicked the abort floating action button
            toogleConfirmButton();
        } else if (view.getId() == R.id.confirm_cancel_drive_button) {
            handleRemoveDrive();
        } else if (view instanceof FloatingActionButton) {
            // The driver has clicked on a passenger FAB
            handlePassengerFabClicked(view);
        }
    }

    // Driver client method
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

    @Override
    public void onStripeTaskCompleted(String result) {
        Timber.d("result is %s", result);
        String[] value = result.split(SPLIT_CHAR);
        switch (value[1]){
            case RESERVE:
                onChargeAmountReserved(value[0]);
                break;
            default:
                break;
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

    public void removeFragment(Fragment fragment) {
        if (fragment != null) {
            mFragmentManager.beginTransaction().remove(fragment).commit();
        }
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
                        if (mCurrentLoggedInUser.getType() == TYPE_DRIVER) {
                            showDriverPickUpFragment(getPassengerRideFromLocalList(passengerRideId));
                        }
                    } else if (geoFenceType.equals(GEOFENCE_TYPE_DROP_OFF)) {
                        if (mCurrentLoggedInUser.getType() == TYPE_DRIVER) {
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
                    removeDriveInformationDialog();
                    if (mActivePassengerRide != null) {
                        showPassengerPickUpFragment(mActivePassengerRide);
                    }
                }
                if (intent.getExtras().getInt(DIALOG_TO_SHOW) == TYPE_DROP_OFF) {
                    handlePassengerDroppedOff();
                }
            }
        }
    }

    public void getPrice(int seats)
    {
        double price;
        if(mBounds!=null) {
            Timber.d("distance bound %s", mBounds.getDistance());
            CalculatePrice calculatePrice = new CalculatePrice(mBounds.getDistance(), seats);
            mPendingDriveRequest.setDistance(((double)mBounds.getDistance())/10000);
            Timber.d("long to double " + ((double)mBounds.getDistance()));
            price = calculatePrice.getPrice();
            mPendingDriveRequest.setPrice(price);
            Timber.d("price is " + price + " " + (int) (price * 100) );
            reserveChargeAmountInBackground((int) (price * 100));
        }
        else
        {
            Toast.makeText(getApplicationContext(),GOOGLE_API_ERROR,Toast.LENGTH_LONG).show();
        }

    }

    public void reserveChargeAmountInBackground(int price) {
        //TODO: Change from asynctask to node.js
        new StripeAsyncTask(createChargeHashMap(mCurrentLoggedInUser.getCustomerId(),price,false),this, RESERVE).execute();

    }

    public void onChargeAmountReserved(String chargeId) {
        Timber.d("charge created with id %s", chargeId);
        if(chargeId == null) {
            Toast.makeText(getApplicationContext(),CARD_ERROR, Toast.LENGTH_LONG).show();
        }
        else
        {
            mMainViewModel.createDriveRequest(mPendingDriveRequest.getTime(),
                    mPendingDriveRequest.getStartLocation(),
                    mPendingDriveRequest.getEndLocation(),
                    mPendingDriveRequest.getExtraPassengers(),
                    mPendingDriveRequest.getPrice(), chargeId,
                    mPendingDriveRequest.getDistance()).observe(
                    this, driveRequest -> {
                        mMainViewModel.setDriveRequestRadiusMultiplier(
                                MainViewModel.DRIVE_REQUEST_DEFAULT_MULTIPLIER);
                        Timber.i("DriveRequest : %s", driveRequest);
                        matchDriveRequest(driveRequest,
                                mMainViewModel.getDriveRequestRadiusMultiplier());
                        mCreateDriveFragment.setDefaultValuesToDialog();
                        mCreateDriveFragment.hideCreateDialogCompletely();
                    });
        }
    }


    // Passenger client method
    private void handlePassengerDroppedOff() {
        mMainViewModel.confirmDropOff(mActivePassengerRide.getId());
    }

    // Passenger client method
    private void handlePassengerRideRemoved(String passengerId) {
        stopForegroundService();
        removeDriveInformationDialog();
        // TODO: stop listening on driver position

        if (!mIsFragmentAdded) {
            addCreateDriveFragment();
        }
        mActivePassengerRide = null;
    }

    public void updateChargeId(String chargeId) {
        onChargeAmountReserved(chargeId);
    }

}