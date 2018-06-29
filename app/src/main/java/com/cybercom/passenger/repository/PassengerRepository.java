package com.cybercom.passenger.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.flows.payment.StripeAsyncTask;
import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.model.Car;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.Route;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.databasemodel.PassengerRide;
import com.cybercom.passenger.repository.databasemodel.utils.DatabaseModelHelper;
import com.cybercom.passenger.repository.networking.DistantMatrixAPIHelper;
import com.cybercom.passenger.repository.networking.model.DistanceMatrixResponse;
import com.cybercom.passenger.route.FetchRoute;
import com.cybercom.passenger.utils.LocationHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.FARE_PER_MILE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.NOSHOW_FEE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.REFUND;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RESERVE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RETRIEVE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.SPLIT_CHAR;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER_REFUND;
import static com.cybercom.passenger.flows.payment.PaymentHelper.createChargeHashMap;
import static com.cybercom.passenger.flows.payment.PaymentHelper.createRefundHashMap;
import static com.cybercom.passenger.flows.payment.PaymentHelper.createTransferHashMap;

public class PassengerRepository implements PassengerRepositoryInterface, StripeAsyncTask.StripeAsyncTaskDelegate {

    //firebase storage for images
    private FirebaseStorage sFirebaseStorage;
    private StorageReference sStorageReference;

    private static final String NOTIFICATION_TOKEN_ID = "notificationTokenId";

    private static final String REFERENCE_NOTIFICATIONS = "notifications";

    private static final String REFERENCE_USERS = "users";
    private static final String REFERENCE_DRIVES = "drives";
    private static final String REFERENCE_CARS = "cars";
    private static final String REFERENCE_DRIVE_REQUESTS = "driveRequests";
    private static final String REFERENCE_USERS_CHILD_TYPE = "type";
    private static final String REFERENCE_DRIVER_ID_BLACK_LIST = "driverIdBlackList";
    private static final String REFERENCE_PASSENGER_RIDE = "passengerRide";
    private static final String REFERENCE_PASSENGER_POSITION = "passengerPosition";
    private static final String IMAGE_LINK = "imageLink";

    private static final String DRIVE_ID = "driveId";
    private static final String DRIVER_ID = "driverId";

    private static final String CHARGE_ID = "chargeId";
    private static final String PASSENGER_ID = "passengerId";

    private static final int DRIVE_REQUEST_MATCH_TIME_THRESHOLD = 15 * 60 * 60 * 1000;
    private static final String NOTIFICATION_TYPE_KEY = "type";
    private static final String KEY_PAYLOAD_DRIVE_REQUEST_ID = "driveRequest";
    private static final String KEY_PAYLOAD_DRIVE_ID = "driveId";
    private static final String KEY_PASSENGER_ID = "passengerId";
    private static final String CURRENT_POSITION = "currentPosition";
    private static final String CURRENT_VELOCITY = "currentVelocity";
    private static final String PICKUP_CONFIRMED = "pickUpConfirmed";
    private static final String PASSENGER_RIDE_CANCELLED = "cancelled";
    private static final String DROPOFF_CONFIRMED = "dropOffConfirmed";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String DISTANCEM = "distance";
    private static final String DURATIONS = "duration";

    // Constants used for matching
    public static final int DEFAULT_DRIVE_REQUEST_RADIUS = 1000;
    public static final int UNDEFINED_ETA = -1;
    public static final int ETA_THRESHOLD = 60 * 10;

    public static final String DISTANCE_TRAVELLED_WITH_PASSENGERS = "distanceTravelledWithPassengers";
    public static final String TOTAL_DISTANCE_TRAVELLED = "totalDistanceTravelled";
    public static final String SWEDISH_MILES_NOT_YET_REIMBURSED = "swedishMilesNotYetReimbursed";

    private static PassengerRepository sPassengerRepository;
    private DatabaseReference mUsersReference;
    private DatabaseReference mDrivesReference;
    private DatabaseReference mPassengerRideReference;
    private DatabaseReference mPassengerPositionReference;
    private DatabaseReference mDriveRequestsReference;
    private DatabaseReference mNotificationsReference;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mCarsReference;
    private MutableLiveData<List<Car>> mCarList;
    private BlockingQueue<Notification> mNotificationQueue = new LinkedBlockingQueue<>();
    private MutableLiveData<Notification> mNotificationLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> mEtaLiveData = new MutableLiveData<>();
    private User mCurrentlyLoggedInUser;
    private MutableLiveData<Location> mDriverCurrentLocation = new MutableLiveData<>();
    private float mDriverCurrentVelocity = 0;

    //private String mCurrentDriveId;
    private static final String BOUNDS = "bounds";
    private static final String NORTHEAST = "northeast";
    private static final String SOUTHWEST = "southwest";
    public static final long MIN_DURATION = 1800;
    private Drive mMatchedDrive;
    ValueEventListener mBestMatchEventListener;

    private static final String FOLDER = "images/";
    private static final String IMAGE_TYPE = ".jpg";
    private Uri mFileUri;

    public static PassengerRepository getInstance() {
        if (sPassengerRepository == null) {
            sPassengerRepository = new PassengerRepository();
        }
        return sPassengerRepository;
    }

    private PassengerRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = firebaseDatabase.getReference(REFERENCE_USERS);
        mDrivesReference = firebaseDatabase.getReference(REFERENCE_DRIVES);
        mPassengerRideReference = firebaseDatabase.getReference(REFERENCE_PASSENGER_RIDE);
        mPassengerPositionReference = firebaseDatabase.getReference(REFERENCE_PASSENGER_POSITION);
        mCarsReference = firebaseDatabase.getReference(REFERENCE_CARS);
        mDriveRequestsReference = firebaseDatabase.getReference(REFERENCE_DRIVE_REQUESTS);
        mNotificationsReference = firebaseDatabase.getReference(REFERENCE_NOTIFICATIONS);
        setStorageCredentials();
    }

    public LiveData<Boolean> validateEmail(String email) {
        final MutableLiveData<Boolean> checkEmail = new MutableLiveData();

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                checkEmail.setValue(task.getResult().getSignInMethods().size() > 0);
            }
        });
        return checkEmail;
    }

    @Override
    public LiveData<User> getUser() {

        final MutableLiveData<User> user = new MutableLiveData<>();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // TODO: Not logged in...
            return null;
        }

        mUsersReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentlyLoggedInUser = dataSnapshot.getValue(User.class);
                user.setValue(mCurrentlyLoggedInUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return user;
    }

    @Override
    public void updateUserType(int type) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mUsersReference.child(firebaseUser.getUid()).child(REFERENCE_USERS_CHILD_TYPE)
                    .setValue(type);
        }
    }

    public void refreshNotificationTokenId(String tokenId) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mUsersReference.child(firebaseUser.getUid())
                    .child(NOTIFICATION_TOKEN_ID).setValue(tokenId);
        }
    }

    private String getTokenId() {
        return FirebaseInstanceId.getInstance().getToken();
    }
/*
    @Override
    public void createUser(String userId, User user) {
        userId = getUserId();
        mUsersReference.child(userId).setValue(user);
    }

    repository.createUser(repository.getUserId(), new User(repository.getUserId(),
                repository.getTokenId(), User.TYPE_PASSENGER,
            mExtras.getString("phone"), mExtras.getString("personalnumber"),
            mExtras.getString("fullname"), null,
            mExtras.getString("gender")
            ));
    */

    public LiveData<FirebaseUser> createUserWithEmailAndPassword(String loginArray) {
        final MutableLiveData<FirebaseUser> userMutableLiveData = new MutableLiveData<>();

        User userLogin = (new Gson()).fromJson(loginArray, User.class);

        //  extraLogin.getParcelable("loginArray");
        mAuth.createUserWithEmailAndPassword(userLogin.getmEmail(), userLogin.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Timber.d("createUserWithEmail:success %s", user);
                        userMutableLiveData.setValue(user);
                        userLogin.setUserId(user.getUid());
                        userLogin.setNotificationTokenId(getTokenId());
                        userLogin.setPassword(null);
                        String imageUriString = userLogin.getImageLink();
                        if (imageUriString != null) {
                            uploadImage(Uri.parse(imageUriString), user.getUid());
                        }
                        mUsersReference.child(user.getUid()).setValue(userLogin);
                    } else {
                        Timber.w("createUserWithEmail:failure %s", (
                                (FirebaseAuthException) task.getException()).getErrorCode());
                       /* if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_WEAK_PASSWORD){
                            SignUpActivity.mPassword.setError(task.getException().getMessage().toString());

                        }else if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_EMAIL_ALREADY_IN_USE){
                            SignUpActivity.mEmail.setError(task.getException().getMessage().toString());
                        }
                        else if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_INVALID_EMAIL){
                            Timber.d("Error %s", ((FirebaseAuthException)task.getException()).getErrorCode());

                            SignUpActivity.mEmail.setError(task.getException().getMessage().toString());
                        }*/
                    }
                });
        return userMutableLiveData;
    }

    public LiveData<FirebaseUser> createUserAddCar(String loginArray, String carArray) {
        final MutableLiveData<FirebaseUser> userMutableLiveData = new MutableLiveData<>();

        User userLogin = (new Gson()).fromJson(loginArray, User.class);
        Car newCar = (new Gson()).fromJson(carArray, Car.class);

        //  extraLogin.getParcelable("loginArray");
        mAuth.createUserWithEmailAndPassword(userLogin.getmEmail(), userLogin.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Timber.d("createUserWithEmail:success %s", user);
                            userMutableLiveData.setValue(user);
                            userLogin.setUserId(user.getUid());
                            userLogin.setNotificationTokenId(getTokenId());
                            userLogin.setPassword(null);
                            String imageUriString = userLogin.getImageLink();
                            if (imageUriString != null) {
                                uploadImage(Uri.parse(imageUriString), user.getUid());
                            }
                            mUsersReference.child(user.getUid()).setValue(userLogin);
                            createCar(newCar.getNumber(), user.getUid(), newCar);
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Timber.w("createUserWithEmail:failure %s",
                                    ((FirebaseAuthException) task.getException()).getErrorCode());
                        }
                       /* if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_WEAK_PASSWORD){
                            SignUpActivity.mPassword.setError(task.getException().getMessage().toString());

                        }else if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_EMAIL_ALREADY_IN_USE){
                            SignUpActivity.mEmail.setError(task.getException().getMessage().toString());
                        }
                        else if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_INVALID_EMAIL){
                            Timber.d("Error %s", ((FirebaseAuthException)task.getException()).getErrorCode());

                            SignUpActivity.mEmail.setError(task.getException().getMessage().toString());
                        }*/
                    }
                });
        return userMutableLiveData;
    }

    private String createDrivesString(
            @NonNull List<com.cybercom.passenger.repository.databasemodel.Drive> drives) {
        StringBuilder drivesString = new StringBuilder();
        for (com.cybercom.passenger.repository.databasemodel.Drive drive : drives) {
            if (!TextUtils.isEmpty(drivesString)) {
                drivesString.append("|");
            }
            drivesString.append(drive.getStartLocation().getLatitude());
            drivesString.append(",");
            drivesString.append(drive.getStartLocation().getLongitude());
        }
        return drivesString.toString();
    }

    private String createPickupString(@NonNull Position pickupPosition) {
        StringBuilder pickupString = new StringBuilder();
        LatLng pick = new LatLng(pickupPosition.getLatitude(), pickupPosition.getLongitude());
        pickupString.append(pick.latitude + "," + pick.longitude);
        return pickupString.toString();
    }

    private ValueEventListener getBestMatchValueEventListener(
            final DriveRequest driveRequest, int radiusMultiplier,String googleApiKey,
            MutableLiveData<Drive> bestDriveMatch) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<com.cybercom.passenger.repository.databasemodel.Drive> candidateDrives
                        = new ArrayList<>();
                final List<Bounds> candidateBounds = new ArrayList<>();
                final List<String> candidateDriveIdList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    com.cybercom.passenger.repository.databasemodel.Drive drive = snapshot.getValue(
                            com.cybercom.passenger.repository.databasemodel.Drive.class);
                    String driveId = snapshot.getKey();
                    if (drive == null) {
                        continue;
                    }
                    if (!isDriveBlackListOk(driveRequest, drive)) {
                        continue;
                    }
                    final Bounds bounds = getBounds(snapshot);
                    if(!isDriveWithinBounds(bounds, driveRequest, radiusMultiplier)) {
                        continue;
                    }
                    candidateDrives.add(drive);
                    candidateBounds.add(bounds);
                    candidateDriveIdList.add(driveId);

                    Timber.d("Match found");
                }

                if (!candidateDrives.isEmpty()) {
                    String drivesString = createDrivesString(candidateDrives);
                    String pickup = createPickupString(driveRequest.getStartLocation());
                    DistantMatrixAPIHelper.getInstance().mMatrixAPIService.getDistantMatrix(
                            drivesString, pickup, googleApiKey).enqueue(
                            new Callback<DistanceMatrixResponse>() {
                                @Override
                                public void onResponse(Call<DistanceMatrixResponse> call,
                                                       Response<DistanceMatrixResponse> response) {
                                    handleDistanceMatrixResponse(candidateBounds, response,
                                            candidateDrives, candidateDriveIdList, bestDriveMatch,
                                            driveRequest, googleApiKey);
                                }

                                @Override
                                public void onFailure(Call<DistanceMatrixResponse> call, Throwable t) {
                                    Timber.d("error");
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }

    public LiveData<Drive> findBestRideMatch(final DriveRequest driveRequest, int radiusMultiplier,
                                             String googleApiKey) {
        final MutableLiveData<Drive> bestDriveMatch = new MutableLiveData<>();
        if (mBestMatchEventListener == null) {
            mBestMatchEventListener = getBestMatchValueEventListener(driveRequest, radiusMultiplier,
                    googleApiKey, bestDriveMatch);
            mDrivesReference.addValueEventListener(mBestMatchEventListener);
        }
        return bestDriveMatch;
    }

    public void cancelBestRideMatch() {
        if (mBestMatchEventListener != null) {
            mDrivesReference.removeEventListener(mBestMatchEventListener);
            mBestMatchEventListener = null;
        }
    }

    private void checkEtaForPossibleDrives(
            int index, List<Bounds> boundsList, List<LatLng> driveRequestLatLngs,
            List<com.cybercom.passenger.repository.databasemodel.Drive> candidateDrives,
            List<String> candidateDriveIdList,
            MutableLiveData<Drive> matchedDriveLiveData, String googleApiKey) {

        if (index >= candidateDrives.size()) {
            return;
        }
        com.cybercom.passenger.repository.databasemodel.Drive drive = candidateDrives.get(index);
        Bounds bounds = boundsList.get(index);
        String driveId = candidateDriveIdList.get(index);

        getPassengerRidesList(index, boundsList, driveRequestLatLngs, candidateDrives,
                candidateDriveIdList, matchedDriveLiveData, googleApiKey, drive, bounds, driveId);
    }

    private void fetchRerouteAndCompareETA(
            int index, List<Bounds> boundsList, List<LatLng> latLngList,
            List<com.cybercom.passenger.repository.databasemodel.Drive> candidateDrives,
            List<String> candidateDriveIdList, MutableLiveData<Drive> matchedDriveLiveData,
            String googleApiKey, com.cybercom.passenger.repository.databasemodel.Drive drive,
            Bounds bounds, String driveId, List<LatLng> passengerLatLngList) {

        LatLng currentPosition = MainActivity.createLatLngFromPosition(drive.getCurrentPosition());
        LatLng endLocation = MainActivity.createLatLngFromPosition(drive.getEndLocation());
        latLngList.addAll(passengerLatLngList);
        new FetchRoute(currentPosition, endLocation, latLngList,
                routes -> {
                    if (routes.size() > 0) {
                        Route route = routes.get(0);
                        long eta = route.getBounds().getDuration();

                        // TODO this is the initial duration.. needs to be updated!!
                        long driveDuration = bounds.getDuration();

                        if (eta < driveDuration + ETA_THRESHOLD) {
                            mUsersReference.child(drive.getDriverId()).
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User driver = dataSnapshot.getValue(User.class);
                                            mMatchedDrive = new Drive(driveId, driver, drive.getTime(),
                                                    drive.getStartLocation(), drive.getEndLocation(),
                                                    drive.getAvailableSeats(), drive.getCurrentPosition(),
                                                    drive.getCurrentVelocity());
                                            matchedDriveLiveData.setValue(mMatchedDrive);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        } else {
                            checkEtaForPossibleDrives(index + 1, boundsList, latLngList,
                                    candidateDrives, candidateDriveIdList,
                                    matchedDriveLiveData, googleApiKey);
                        }
                    }
                }, googleApiKey);
    }


    private void handleDistanceMatrixResponse(
            List<Bounds> listBounds,
            Response<DistanceMatrixResponse> response,
            List<com.cybercom.passenger.repository.databasemodel.Drive> listDriveKey,
            List<String> candidateDriveIdList,
            MutableLiveData<Drive> bestDriveMatch,
            final DriveRequest driveRequest,
            String googleApiKey) {
        if (!response.isSuccessful()) {
            return;
        }

        int etaListSize = DistantMatrixAPIHelper.getRowsCount(response);
        if (etaListSize == 0) {
            return;
        }
        List<com.cybercom.passenger.repository.databasemodel.Drive> candidateDrives =
                new ArrayList<>();
        List<Bounds> candidateBounds = new ArrayList<>();
        List<String> candidateIdList = new ArrayList<>();
        for (int i = 0; i < etaListSize; i++) {
            long eta = DistantMatrixAPIHelper.getDurationFromResponse(response, i, 0);
            if (eta < MIN_DURATION) {
                candidateDrives.add(listDriveKey.get(i));
                candidateBounds.add(listBounds.get(i));
                candidateIdList.add(candidateDriveIdList.get(i));
            }
        }
        //check for eta with waypoints for all drives within threshold
        if (candidateDrives.isEmpty()) {
            return;
        }

        try {
            List<LatLng> latLngs = new ArrayList();
            //TODO move to common place...
            latLngs.add(MainActivity.createLatLngFromPosition(
                    driveRequest.getStartLocation()));
            latLngs.add(MainActivity.createLatLngFromPosition(driveRequest.getEndLocation()));

            checkEtaForPossibleDrives(0, candidateBounds, latLngs, candidateDrives, candidateIdList,
                    bestDriveMatch, googleApiKey);
        }
        catch(Exception e) {
            Timber.e(e.getLocalizedMessage());
        }


    }

    private boolean isDriveBlackListOk(
            @NonNull DriveRequest driveReq,
            @NonNull com.cybercom.passenger.repository.databasemodel.Drive drive) {
        return !driveReq.getDriverIdBlackList().contains(drive.getDriverId());

    }

    private Bounds getBounds(DataSnapshot snapshot) {
        if (!snapshot.hasChild(BOUNDS)) {
            return null;
        }
        Object northEastLat = snapshot.child(BOUNDS).child(NORTHEAST).child(LATITUDE).getValue();
        Object northEastLong = snapshot.child(BOUNDS).child(NORTHEAST).child(LONGITUDE).getValue();
        Object southWestLat = snapshot.child(BOUNDS).child(SOUTHWEST).child(LATITUDE).getValue();
        Object southWestLong = snapshot.child(BOUNDS).child(SOUTHWEST).child(LONGITUDE).getValue();
        Object distance = snapshot.child(BOUNDS).child(DISTANCEM).getValue();
        Object duration = snapshot.child(BOUNDS).child(DURATIONS).getValue();
        if (northEastLat == null || northEastLong == null || southWestLat == null
                || southWestLong == null || distance == null || duration == null) {
            return null;
        }

        try {
            return new Bounds(Double.parseDouble(northEastLat.toString()),
                    Double.parseDouble(northEastLong.toString()),
                    Double.parseDouble(southWestLat.toString()),
                    Double.parseDouble(southWestLong.toString()),
                    Long.parseLong(distance.toString()),
                    Long.parseLong(duration.toString()));


        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isDriveWithinBounds(Bounds bounds, DriveRequest driveRequest,
                                        int radiusMultiplier) {
        return bounds != null && Bounds.isPositionWithinBounds(bounds,
                driveRequest.getStartLocation(), radiusMultiplier) &&
                Bounds.isPositionWithinBounds(bounds, driveRequest.getEndLocation(),
                        radiusMultiplier);

    }

    public Drive getMatchedDrive() {
        return mMatchedDrive;
    }

    public void sendNotification(Notification notification) {
        com.cybercom.passenger.repository.databasemodel.Notification dataBaseNotification =
                DatabaseModelHelper.convertNotification(notification);

        mNotificationsReference.push().setValue(dataBaseNotification);
    }

    public LiveData<Notification> receiveIncomingNotifications() {
        return mNotificationLiveData;
    }

    /**
     * Rebuild the notification (to the ModelView model) from payload coming from push-notification
     * and add it to the notification queue
     *
     * @param payload The push-notification's payload
     */
    public void setIncomingNotification(final Map<String, String> payload) {

        final String driveId = payload.get(KEY_PAYLOAD_DRIVE_ID);
        final String driveRequestId = payload.get(KEY_PAYLOAD_DRIVE_REQUEST_ID);

        // Fetch the Drive
        DatabaseReference drive = mDrivesReference.child(driveId);
        drive.addListenerForSingleValueEvent(getEventListenerToSetIncomingNotification(
                driveId, driveRequestId, payload));
    }

    private ValueEventListener getEventListenerToSetIncomingNotification(
            @NonNull final String driveId, @NonNull final String driveRequestId,
            final Map<String, String> payload) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final com.cybercom.passenger.repository.databasemodel.Drive dbDrive =
                        dataSnapshot.getValue(
                                com.cybercom.passenger.repository.databasemodel.Drive.class);

                if (dbDrive == null || dbDrive.getDriverId() == null) {
                    Timber.e("dbDrive was null");
                    return;
                }
                // Fetch the Driver (User)
                DatabaseReference dbRefDriveId = mUsersReference.child(dbDrive.getDriverId());
                dbRefDriveId.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User driver = dataSnapshot.getValue(User.class);
                        if (driver == null) {
                            return;
                        }
                        // Fetch the DriverRequest
                        Timber.i("fetch driveRequest : %s", driveRequestId);
                        final DatabaseReference driveReq = mDriveRequestsReference.child(
                                driveRequestId);
                        driveReq.addListenerForSingleValueEvent(
                                getEventListnenerToFetchDriveRequestAndAddNotification(driveId,
                                        driver, dbDrive, driveReq, driveRequestId, payload));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.i("Failed to fetch user: driver: %s"
                                , databaseError.toString());
                    }
                });
            }

            @SuppressLint("TimberArgCount")
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e("getDriver:onCancelled", databaseError.toException());
            }
        };
    }


    private ValueEventListener getEventListnenerToFetchDriveRequestAndAddNotification(
            @NonNull final String driveId, @NonNull User driver,
            @NonNull com.cybercom.passenger.repository.databasemodel.Drive dbDrive,
            @NonNull final DatabaseReference driveReq,
            @NonNull String driveRequestId, final Map<String, String> payload) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final com.cybercom.passenger.repository.databasemodel.DriveRequest dBdriveRequest =
                        dataSnapshot.getValue(
                                com.cybercom.passenger.repository.databasemodel.DriveRequest.class);

                if (dBdriveRequest == null) {
                    Timber.e("dBdriveRequest was null");
                    return;
                }
                GenericTypeIndicator<List<String>> genTypeIndicator =
                        new GenericTypeIndicator<List<String>>() {
                        };
                List<String> list = dataSnapshot.child(REFERENCE_DRIVER_ID_BLACK_LIST)
                        .getValue(genTypeIndicator);
                if (list == null) {
                    list = new ArrayList<>();
                }

                // If it is a Reject-notification add driverId to the driver-request's blacklist
                int notificationType = Integer.valueOf(payload.get(NOTIFICATION_TYPE_KEY));
                if (notificationType == Notification.REJECT_PASSENGER) {
                    list.add(driver.getUserId());
                    List<Object> objectList = new ArrayList(list);
                    dBdriveRequest.setDriverIdBlackList(objectList);
                    driveReq.setValue(dBdriveRequest);
                } else {
                    dBdriveRequest.setDriverIdBlackList(new ArrayList(list));
                }

                // Fetch the passenger (User)
                DatabaseReference passengerReq = mUsersReference.child(
                        dBdriveRequest.getPassengerId());
                passengerReq.addListenerForSingleValueEvent(
                        getEventListenerToAddNotification(driveId, driver, dbDrive,
                                driveRequestId, dBdriveRequest, payload));
            }

            @SuppressLint("TimberArgCount")
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.i("Failed to fetch DriveRequest: %s"
                        , databaseError.toString());
            }
        };
    }

    private ValueEventListener getEventListenerToAddNotification(
            @NonNull final String driveId, @NonNull User driver,
            @NonNull com.cybercom.passenger.repository.databasemodel.Drive dbDrive,
            @NonNull String driveRequestId,
            @NonNull final com.cybercom.passenger.repository.databasemodel.DriveRequest dbDriveRequest,
            final Map<String, String> payload) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User passenger = dataSnapshot.getValue(User.class);

                Drive drive = new Drive(driveId, driver,
                        dbDrive.getTime(), dbDrive.getStartLocation(),
                        dbDrive.getEndLocation(), dbDrive.getAvailableSeats(),
                        dbDrive.getCurrentPosition(), dbDrive.getCurrentVelocity());
                DriveRequest driveRequest = new DriveRequest(driveRequestId,
                        passenger, dbDriveRequest.getTime(), dbDriveRequest.getStartLocation(),
                        dbDriveRequest.getEndLocation(), dbDriveRequest.getExtraPassengers(),
                        dbDriveRequest.getPrice(), dbDriveRequest.getChargeId(),
                        dbDriveRequest.getDriverIdBlackList(), dbDriveRequest.getDistance());
                addToNotificationQueue(new Notification(Integer.parseInt(
                        payload.get(NOTIFICATION_TYPE_KEY)), driveRequest, drive));
            }

            @SuppressLint("TimberArgCount")
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.i("Failed to fetch user: passenger: %s",
                        databaseError.toString());
            }
        };
    }

    private void addToNotificationQueue(Notification notification) {
        Timber.d("Add %s to notification queue", notification.toString());
        mNotificationQueue.add(notification);
        if (mNotificationQueue.size() == 1) {
            mNotificationLiveData.postValue(mNotificationQueue.peek());
        }
        Timber.d("Notifcation queue size after add: %d", mNotificationQueue.size());
    }

    public void getNextNotification() {
        Notification n = mNotificationQueue.poll();
        mNotificationLiveData.postValue(mNotificationQueue.peek());
        Timber.d("Notifcation queue size after get: %d", mNotificationQueue.size());
    }

    public LiveData<Drive> createDrive(long time, Position startLocation, Position endLocation,
                                       int availableSeats, Bounds bounds) {
        final MutableLiveData<Drive> driveMutableLiveData = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uId = firebaseUser.getUid();

            final com.cybercom.passenger.repository.databasemodel.Drive dbDrive =
                    new com.cybercom.passenger.repository.databasemodel.Drive(uId, time,
                            startLocation, endLocation, availableSeats, startLocation, 0f);
            final DatabaseReference ref = mDrivesReference.push();
            final String driveId = ref.getKey();
            ref.setValue(dbDrive);
            //adding bounds for drive

            Map<String, Object> neBounds = new HashMap<>();
            neBounds.put(LATITUDE, bounds.getNorthEastLatitude());
            neBounds.put(LONGITUDE, bounds.getNorthEastLongitude());
            Map<String, Object> swBounds = new HashMap<>();
            swBounds.put(LATITUDE, bounds.getSouthWestLatitude());
            swBounds.put(LONGITUDE, bounds.getSouthWestLongitude());
            mDrivesReference.child(driveId).child(BOUNDS).child(SOUTHWEST).setValue(swBounds);
            mDrivesReference.child(driveId).child(BOUNDS).child(NORTHEAST).setValue(neBounds);

            mDrivesReference.child(driveId).child(BOUNDS).child(DISTANCEM).setValue(
                    bounds.getDistance());
            mDrivesReference.child(driveId).child(BOUNDS).child(DURATIONS).setValue(
                    bounds.getDuration());

            //--------
            mUsersReference.child(firebaseUser.getUid()).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            Drive drive = new Drive(driveId, user, dbDrive.getTime(),
                                    dbDrive.getStartLocation(), dbDrive.getEndLocation(),
                                    dbDrive.getAvailableSeats(), dbDrive.getCurrentPosition(),
                                    dbDrive.getCurrentVelocity());
                            driveMutableLiveData.setValue(drive);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        } else {
            // Not logged in
            driveMutableLiveData.setValue(null);
        }
        return driveMutableLiveData;
    }

    //reserve charge before driverequest is created
    public void reserveChargeAmountInBackground(int price, String customerId, DriveRequest pendingDriveRequest){
       // mPendingDriveRequest = pendingDriveRequest;

        new StripeAsyncTask(createChargeHashMap(customerId, price,false),this, RESERVE).execute();
    }

    public LiveData<DriveRequest> createDriveRequest(long time, Position startLocation,
                                                     Position endLocation, int availableSeats, double price, String chargeId, double distance) {


        getAmountInCharge(chargeId);
        final MutableLiveData<DriveRequest> driveRequestMutableLiveData = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            // User is not logged in
            driveRequestMutableLiveData.setValue(null);
            return driveRequestMutableLiveData;
        }

        String uId = firebaseUser.getUid();

        final com.cybercom.passenger.repository.databasemodel.DriveRequest dbDriveRequest =
                new com.cybercom.passenger.repository.databasemodel.DriveRequest(uId, time,
                        startLocation, endLocation, availableSeats, price, chargeId, new ArrayList<>(), distance);
        final DatabaseReference ref = mDriveRequestsReference.push();
        final String driveRequestId = ref.getKey();
        ref.setValue(dbDriveRequest);

        mUsersReference.child(firebaseUser.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        DriveRequest driveRequest = new DriveRequest(driveRequestId, user,
                                dbDriveRequest.getTime(), dbDriveRequest.getStartLocation(),
                                dbDriveRequest.getEndLocation(), dbDriveRequest.getExtraPassengers(),
                                dbDriveRequest.getPrice(),
                                dbDriveRequest.getChargeId(),
                                dbDriveRequest.getDriverIdBlackList(),
                                dbDriveRequest.getDistance());
                        driveRequestMutableLiveData.setValue(driveRequest);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        return driveRequestMutableLiveData;
    }

    public void removeCurrentDrive(String driveId, OnCompleteListener onCompleteListener) {
        Task task = mDrivesReference.child(driveId).removeValue();
        task.addOnCompleteListener(onCompleteListener);
    }

    public void removePassengerRide(String passengerRideId, OnCompleteListener onCompleteListener) {
        Task task = mPassengerRideReference.child(passengerRideId).removeValue();
        task.addOnCompleteListener(onCompleteListener);
    }

    private DatabaseReference getCarsReference() {
        mCarList = new MutableLiveData<>();
        return mCarsReference;
    }

    @Override
    public void createCar(String carId, String userId, Car car) {
        getCarsReference().child(userId).child(carId).setValue(car);
    }

    @Override
    public void removeCar(String carId, String userId) {

        getCarsReference().child(userId).child(carId).removeValue();
    }

    public void onChangeCarDetails() {
        getCarsReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                getAllTask(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAllTask(DataSnapshot dataSnapshot) {
        List<Car> allCar = new ArrayList<>();
        Map<String, Object> objectMap = (HashMap<String, Object>)
                dataSnapshot.getValue();

        for (Object obj : objectMap.values()) {
            if (obj instanceof Map) {
                Map<String, Object> mapObj = (Map<String, Object>) obj;
                try {
                    Car match = new Car(mapObj.get("number").toString(),
                            mapObj.get("model").toString(),
                            mapObj.get("year").toString(),
                            mapObj.get("color").toString());
                    allCar.add(match);
                } catch (Exception e) {
                    Timber.e(e.getLocalizedMessage());
                }
            }
        }

        mCarList.setValue(allCar);
    }

    public MutableLiveData<List<Car>> getUpdatedCarList() {
        return mCarList;
    }

    public void updateDriveCurrentLocation(String driveId, Location location) {
        if (driveId != null) {
            mDriverCurrentLocation.setValue(location);
            mDrivesReference.child(driveId).child(CURRENT_POSITION).setValue(
                    LocationHelper.convertLocationToPosition(location));
        }
    }

    public void updateDriveCurrentVelocity(String driveId, float velocity) {
        if (driveId != null) {
            mDriverCurrentVelocity = velocity;
            mDrivesReference.child(driveId).child(CURRENT_VELOCITY).setValue(velocity);
        }
    }

    public void updateDriveCurrentDuration(@NonNull String driveId, long duration, long distance) {
        mDrivesReference.child(driveId).child(BOUNDS).child(DURATIONS).setValue(duration);
        mDrivesReference.child(driveId).child(BOUNDS).child(DISTANCEM).setValue(distance);
    }

    public void updateDriveDistanceTravelled(@NonNull String driveId, float totalDistanceTravelled) {
        mDrivesReference.child(driveId).child(TOTAL_DISTANCE_TRAVELLED).setValue(totalDistanceTravelled);
    }
    public void updateDriveDistanceWithPassengers(@NonNull String driveId, float distanceTravelledWithPassengers) {
        mDrivesReference.child(driveId).child(DISTANCE_TRAVELLED_WITH_PASSENGERS).setValue(distanceTravelledWithPassengers);
    }
    public void updateDriveSwedishMilesNotYetReimbursed(@NonNull String driveId, int distanceNotYetReimbursed) {
        mDrivesReference.child(driveId).child(SWEDISH_MILES_NOT_YET_REIMBURSED).setValue(distanceNotYetReimbursed);
    }

    public LiveData<Float> getDriveDistanceTravelled(String driveId) {
        MutableLiveData<Float> distanceTravelledMutableLiveData = new MutableLiveData<>();

        mDrivesReference.child(driveId).child(TOTAL_DISTANCE_TRAVELLED)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        distanceTravelledMutableLiveData.setValue(dataSnapshot.getValue(Float.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        return distanceTravelledMutableLiveData;
    }

    public LiveData<Float> getDriveDistanceWithPassengers(String driveId) {
        MutableLiveData<Float> distanceTravelledWithPassengersMutableLiveData = new MutableLiveData<>();

        mDrivesReference.child(driveId).child(DISTANCE_TRAVELLED_WITH_PASSENGERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        distanceTravelledWithPassengersMutableLiveData
                                .setValue(dataSnapshot.getValue(Float.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        return distanceTravelledWithPassengersMutableLiveData;
    }

    public LiveData<Integer> getDriveSwedishMilesNotYetReimbursed(String driveId) {
        MutableLiveData<Integer> swedishMilesNotYetReimbursedLiveData = new MutableLiveData<>();

        mDrivesReference.child(driveId).child(SWEDISH_MILES_NOT_YET_REIMBURSED)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        swedishMilesNotYetReimbursedLiveData.setValue(dataSnapshot.getValue(Integer.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        return swedishMilesNotYetReimbursedLiveData;
    }



    public void updatePassengerRideCurrentLocation(Location location) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            return;
        }
        String uId = firebaseUser.getUid();
        mPassengerPositionReference.child(uId).setValue(LocationHelper.convertLocationToPosition(location));
    }

    public LiveData<com.cybercom.passenger.model.PassengerRide> createPassengerRide(
            Drive drive, Position pickUpLocation, Position dropOffLocation, String startAddress,
            String endAddress, String chargeId, double price) {
        final MutableLiveData<com.cybercom.passenger.model.PassengerRide>
                passengerRideMutableLiveData = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uId = firebaseUser.getUid();

            final DatabaseReference passengerPositionRef = mPassengerPositionReference.push();

            // Let the initial position be null
            passengerPositionRef.setValue(null);

            final com.cybercom.passenger.repository.databasemodel.PassengerRide dbPassengerRide =
                    new com.cybercom.passenger.repository.databasemodel.PassengerRide(drive.getId(),
                            uId, pickUpLocation, dropOffLocation, false, false, startAddress,
                            endAddress, chargeId, price);
            final DatabaseReference passengerRideRef = mPassengerRideReference.push();
            final String passengerRideId = passengerRideRef.getKey();

            passengerRideRef.setValue(dbPassengerRide);

            mUsersReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(
                    getEventListenerToBuildPassengerRide(passengerRideId, pickUpLocation,
                            dropOffLocation, drive, passengerRideMutableLiveData, startAddress,
                            endAddress, chargeId, price));
        }
        return passengerRideMutableLiveData;
    }

    private ValueEventListener getEventListenerToBuildPassengerRide(
            String passengerRideId, Position pickUpLocation, Position dropOffLocation, Drive drive,
            MutableLiveData<com.cybercom.passenger.model.PassengerRide>
                    passengerRideMutableLiveData, String startAddress, String endAddress,
            String chargeId, double price) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User passenger = dataSnapshot.getValue(User.class);
                com.cybercom.passenger.model.PassengerRide passengerRide =
                        new com.cybercom.passenger.model.PassengerRide(
                                passengerRideId, drive, passenger, pickUpLocation, dropOffLocation,
                                false, false, startAddress, endAddress,
                                false, chargeId, price);
                passengerRideMutableLiveData.setValue(passengerRide);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void getPassengerRidesList(
            int index, List<Bounds> boundsList, List<LatLng> latLngs,
            List<com.cybercom.passenger.repository.databasemodel.Drive> candidateDrives,
            List<String> candidateDriveIdList, MutableLiveData<Drive> matchedDriveLiveData,
            String googleApiKey, com.cybercom.passenger.repository.databasemodel.Drive drive,
            Bounds bounds, String driveId) {

        mPassengerRideReference.orderByChild(DRIVE_ID).equalTo(driveId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LatLng> passangersLatLngList = new ArrayList();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PassengerRide passengerRide = snapshot.getValue(PassengerRide.class);
                    if (passengerRide.isCancelled()) {
                        continue;
                    }
                    if (!passengerRide.isPickUpConfirmed()) {
                        passangersLatLngList.add(MainActivity.createLatLngFromPosition(
                                passengerRide.getPickUpPosition()));
                    }
                    if (!passengerRide.isDropOffConfirmed()) {
                        passangersLatLngList.add(MainActivity.createLatLngFromPosition(
                                passengerRide.getPickUpPosition()));
                    }
                }

                fetchRerouteAndCompareETA(index, boundsList, latLngs, candidateDrives,
                        candidateDriveIdList, matchedDriveLiveData, googleApiKey, drive, bounds,
                        driveId, passangersLatLngList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public LiveData<com.cybercom.passenger.model.PassengerRide> getPassengerRides(String driveId) {
        MutableLiveData<com.cybercom.passenger.model.PassengerRide> passengerRidesLiveData =
                new MutableLiveData<>();

        Timber.i("getPassengerRides %s", driveId);
        mPassengerRideReference.orderByChild(DRIVE_ID).equalTo(driveId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PassengerRide passengerRide = snapshot.getValue(PassengerRide.class);
                            Timber.d("result: %s", passengerRide);
                            if (passengerRide != null) {
                                final String passengerRideId = snapshot.getKey();

                                // Fetch the Passenger
                                DatabaseReference dbRefPassengerId = mUsersReference.child(
                                        passengerRide.getPassengerId());
                                dbRefPassengerId.addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                final User passenger = dataSnapshot.getValue(User.class);
                                                if (passenger == null) {
                                                    return;
                                                }
                                                // Fetch the Drive
                                                final DatabaseReference driveRef = mDrivesReference.child(
                                                        driveId);
                                                driveRef.addListenerForSingleValueEvent(
                                                        getEventListenerToFetchDriveAndBuildPassengerRide(
                                                                passengerRideId, passengerRide, passenger,
                                                                passengerRidesLiveData));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Timber.i("Failed to fetch user: driver: %s"
                                                        , databaseError.toString());
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
        return passengerRidesLiveData;
    }

    public LiveData<List<PassengerRide>> getPassengerRidesForService(String driveId) {
        MutableLiveData<List<PassengerRide>> passengerRidesListLiveData =
                new MutableLiveData<>();

        Timber.i("getPassengerRides %s", driveId);
        mPassengerRideReference.orderByChild(DRIVE_ID).equalTo(driveId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<PassengerRide> passengerRidesList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            passengerRidesList.add(snapshot.getValue(PassengerRide.class));
                        }
                        passengerRidesListLiveData.setValue(passengerRidesList);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
        return passengerRidesListLiveData;
    }

    private ValueEventListener getEventListenerToFetchDriveAndBuildPassengerRide(
            String passengerRideId, PassengerRide passengerRide, User passenger,
            MutableLiveData<com.cybercom.passenger.model.PassengerRide>
                    passengerRideMutableLiveData) {

        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                com.cybercom.passenger.repository.databasemodel.Drive drive =
                        dataSnapshot.getValue(com.cybercom.passenger.repository.databasemodel.Drive.class);

                final String driveId = dataSnapshot.getKey();

                if (drive != null && drive.getDriverId() != null) {
                    mUsersReference.child(drive.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User driver = dataSnapshot.getValue(User.class);

                            Drive convertedDrive = new Drive(driveId, driver,
                                    drive.getTime(),
                                    drive.getStartLocation(), drive.getEndLocation(),
                                    drive.getAvailableSeats(), drive.getCurrentPosition(),
                                    drive.getCurrentVelocity());

                            com.cybercom.passenger.model.PassengerRide convertedPassengerRide =
                                    new com.cybercom.passenger.model.PassengerRide(
                                            passengerRideId, convertedDrive, passenger,
                                            passengerRide.getPickUpPosition(),
                                            passengerRide.getDropOffPosition(),
                                            passengerRide.isPickUpConfirmed(),
                                            passengerRide.isDropOffConfirmed(),
                                            passengerRide.getStartAddress(),
                                            passengerRide.getEndAddress(),
                                            passengerRide.isCancelled(),
                                            passengerRide.getChargeId(),
                                            passengerRide.getPrice());
                            passengerRideMutableLiveData.setValue(convertedPassengerRide);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e("database error: %s", databaseError);
            }
        };
    }

    public LiveData<Position> getDriverPosition(String driveId) {
        MutableLiveData<Position> driverPositionLiveData = new MutableLiveData<>();

        mDrivesReference.child(driveId).child(CURRENT_POSITION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Position position = dataSnapshot.getValue(Position.class);
                driverPositionLiveData.setValue(position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                driverPositionLiveData.setValue(null);
            }
        });
        return driverPositionLiveData;
    }

    public LiveData<Float> getDriverVelocity(String driveId) {
        MutableLiveData<Float> driverVelocityLiveData = new MutableLiveData<>();

        mDrivesReference.child(driveId).child(CURRENT_VELOCITY)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        driverVelocityLiveData.setValue(dataSnapshot.getValue(Float.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        driverVelocityLiveData.setValue(null);
                    }
                });
        return driverVelocityLiveData;
    }

    public MutableLiveData<Location> getDriverCurrentLocation() {
        return mDriverCurrentLocation;
    }

    public float getDriverCurrentVelocity() {
        return mDriverCurrentVelocity;
    }

    public LiveData<Position> getPassengerPosition(String userId) {
        MutableLiveData<Position> passengerRidesLiveData =
                new MutableLiveData<>();

        mPassengerPositionReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Position position = dataSnapshot.getValue(Position.class);
                Timber.d("result: %s", position);
                if (position != null) {
                    passengerRidesLiveData.setValue(position);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        return passengerRidesLiveData;
    }

    public LiveData<Drive> getActiveDrive() {
        MutableLiveData<Drive> driveMutableLiveData = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // TODO: Not logged in...
            return driveMutableLiveData;
        }

        mUsersReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentlyLoggedInUser = dataSnapshot.getValue(User.class);
                if (mCurrentlyLoggedInUser != null) {
                    mDrivesReference.orderByChild(DRIVER_ID)
                        .equalTo(mCurrentlyLoggedInUser.getUserId())
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                com.cybercom.passenger.repository.databasemodel.Drive dbDrive =
                                        dataSnapshot.getValue(com.cybercom.passenger.repository.databasemodel.Drive.class);
                                driveMutableLiveData.setValue(new Drive(dataSnapshot.getKey(),
                                        mCurrentlyLoggedInUser, dbDrive.getTime(),
                                        dbDrive.getStartLocation(), dbDrive.getEndLocation(),
                                        dbDrive.getAvailableSeats(), dbDrive.getCurrentPosition(),
                                        dbDrive.getCurrentVelocity()));
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                driveMutableLiveData.setValue(null);
                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot,
                                                     @Nullable String s) {
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }

                        });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        return driveMutableLiveData;
    }

    public LiveData<com.cybercom.passenger.model.PassengerRide> getActivePassengerRide() {
        MutableLiveData<com.cybercom.passenger.model.PassengerRide> passengerRideMutableLiveData
                = new MutableLiveData<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // TODO: Not logged in...
            return passengerRideMutableLiveData;
        }

        mUsersReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mCurrentlyLoggedInUser = dataSnapshot.getValue(User.class);
                if (mCurrentlyLoggedInUser != null) {
                    mPassengerRideReference.orderByChild(KEY_PASSENGER_ID)
                            .equalTo(mCurrentlyLoggedInUser.getUserId())
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    com.cybercom.passenger.repository.databasemodel.PassengerRide dbPassengerRide =
                                            dataSnapshot.getValue(com.cybercom.passenger.repository.databasemodel.PassengerRide.class);

                                    final DatabaseReference driveRef = mDrivesReference.child(
                                            dbPassengerRide.getDriveId());
                                    driveRef.addListenerForSingleValueEvent(getEventListenerToFetchDriveAndBuildPassengerRide(dataSnapshot.getKey(),
                                            dbPassengerRide, mCurrentlyLoggedInUser, passengerRideMutableLiveData));
                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                                    passengerRideMutableLiveData.setValue(null);
                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        return passengerRideMutableLiveData;
    }

    public LiveData<PassengerRide> getPassengerRideById(String passengerRideId) {
        MutableLiveData<PassengerRide> passengerRideLiveData = new MutableLiveData<>();

        mPassengerRideReference.child(passengerRideId).
            addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PassengerRide passengerRide = dataSnapshot.getValue(PassengerRide.class);
                    passengerRideLiveData.setValue(passengerRide);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        return passengerRideLiveData;
    }

    public void updateETA(long etaSeconds) {
        if (etaSeconds == PassengerRepository.UNDEFINED_ETA) {
            mEtaLiveData.setValue(PassengerRepository.UNDEFINED_ETA);
        } else {
            mEtaLiveData.setValue((int) (etaSeconds / 60));
        }
    }

    public LiveData<Integer> getETAInMin() {
        return mEtaLiveData;
    }


    /**
     * Gets a drive given a driveId. The method returns a live data that will be updated once the
     * drive is fetched from Firebase
     * @param driveId The id of the drive
     * @return MutableLiveData<Drive> holding the drive.
     */
    public MutableLiveData<Drive> getDrive(String driveId) {
        MutableLiveData<Drive> driveLiveData = new MutableLiveData<>();
        mDrivesReference.orderByKey().equalTo(driveId).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    com.cybercom.passenger.repository.databasemodel.Drive drive;
                    drive = snapshot.getValue(
                            com.cybercom.passenger.repository.databasemodel.Drive.class);
                    Drive modelDrive = new Drive(driveId, null, 0, drive.getStartLocation(),
                            drive.getEndLocation(), drive.getAvailableSeats(),
                            drive.getCurrentPosition(), drive.getCurrentVelocity());
                    driveLiveData.setValue(modelDrive);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.d(databaseError.getMessage());
            }
        });
        return driveLiveData;
    }

    public void confirmPickUp(String passengerRideId) {
        mPassengerRideReference.child(passengerRideId).child(PICKUP_CONFIRMED).setValue(true);
    }

    public void passengerConfirmPickUp(String driveId) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String passengerId = firebaseUser.getUid();
            mPassengerRideReference.orderByChild(DRIVE_ID).equalTo(driveId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        PassengerRide passengerRide = snapshot.getValue(PassengerRide.class);
                        String passengerRideId = snapshot.getKey();
                        if (passengerRide.getPassengerId().equals(passengerId)) {
                            mPassengerRideReference.child(passengerRideId).child(PICKUP_CONFIRMED).
                                    setValue(true);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Returns a livedata containing a list of remaining latlng for the current drive.
     * That is, if the passenger has been picked up, the passengers pickup location is not added and
     * so on.
      * @param driveId The id of the drive.
     * @return
     */
    public MutableLiveData<List<LatLng>> getPassengersRemainingLatLngs(
            String driveId) {
        MutableLiveData<List<LatLng>> passengersLatLngs = new MutableLiveData<>();
        mPassengerRideReference.orderByChild(DRIVE_ID).equalTo(driveId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LatLng> latLngs = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PassengerRide passengerRide = snapshot.getValue(PassengerRide.class);
                    if (!passengerRide.isPickUpConfirmed()) {
                        latLngs.add(MainActivity.createLatLngFromPosition(
                                passengerRide.getPickUpPosition()));
                    }
                    if (!passengerRide.isDropOffConfirmed()) {
                        latLngs.add(MainActivity.createLatLngFromPosition(
                                passengerRide.getDropOffPosition()));
                    }
                }
                passengersLatLngs.setValue(latLngs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return passengersLatLngs;
    }

    public void confirmDropOff(String passengerRideId) {
        mPassengerRideReference.child(passengerRideId).child(DROPOFF_CONFIRMED).setValue(true);
    }

    public void cancelPassengerRide(String passengerRideId) {
        mPassengerRideReference.child(passengerRideId).child(PASSENGER_RIDE_CANCELLED).setValue(
                true);
    }

    public void getAmountInCharge(String chargeId) {
        new StripeAsyncTask(null,this,RETRIEVE).execute(chargeId);
    }

    public void refundFull(String chargeId)
    {
        new StripeAsyncTask(createRefundHashMap(chargeId),this,REFUND).execute();
    }

    @Override
    public void onStripeTaskCompleted(String result) {
        Timber.d("stripe result is %s", result);
        String[] value = result.split(SPLIT_CHAR);
        switch (value[1]){
            case RETRIEVE:
                onChargeAmountRetrieved(value[0]);
                break;
            case REFUND:
                onRefund(value[0]);
                break;
            case TRANSFER:
                onTransfer(value[0]);
                break;
            case RESERVE:
                onReserve(value[0]);
                break;
            case TRANSFER_REFUND:
                onTransferRefund(value[0]);
                break;
            default:
                break;
        }
    }

    private void onChargeAmountRetrieved(String value) {
        Timber.d("stripe amount reserved is %s", value);
    }

    private void onRefund(String value) {
        Timber.d("stripe amount refunded is %s", value);
    }

    public void transferRefund(String chargeId, String customerId){
        new StripeAsyncTask(createTransferHashMap(chargeId, NOSHOW_FEE,
                customerId), this,
                TRANSFER_REFUND).execute();
    }

    private void onTransfer(String value){
        Timber.d("stripe amount transfered is %s", value);
    }

    private void onReserve(String value){
        Timber.d("stripe amount reserved is %s", value);
    }

    private void onTransferRefund(String value){
        Timber.d("stripe transfer refund is %s", value);
    }

    //This method has to be called when the last passenger has been dropped off
    public void paymentTransfer(int dist, HashMap<String, Double> chargesMap){
        double driverReimbursement = dist * FARE_PER_MILE ;
        
        mCurrentlyLoggedInUser.getCustomerId();
        Timber.d("stripe payment to driver is %s to account id: %s", driverReimbursement,
                mCurrentlyLoggedInUser.getCustomerId());
        for(Map.Entry<String, Double> entry : chargesMap.entrySet()) {
            String chargeId = entry.getKey();
            double price = entry.getValue();
            if(price > driverReimbursement){
                new StripeAsyncTask(createTransferHashMap(chargeId, (int)driverReimbursement * 100,
                        mCurrentlyLoggedInUser.getCustomerId()), this, TRANSFER).execute();
                driverReimbursement = 0;
            }else{
                new StripeAsyncTask(createTransferHashMap(chargeId, (int)price * 100,
                        mCurrentlyLoggedInUser.getCustomerId()), this, TRANSFER).execute();
                driverReimbursement = driverReimbursement - price;
            }
        }
    }

    private void setStorageCredentials()
    {
        sFirebaseStorage = FirebaseStorage.getInstance();
        sStorageReference = sFirebaseStorage.getReference();
    }

    private StorageReference getStorageReference()
    {
        return sStorageReference;
    }

    private void uploadImage(Uri filePath, String userId) {
        if(filePath != null)
        {
            StorageReference ref = getStorageReference().child(FOLDER + userId + IMAGE_TYPE);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressLint("TimberArgCount")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Timber.d("upload file successful", taskSnapshot.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("TimberArgCount")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Timber.d("upload file error", e.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            Timber.d("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    public LiveData<Uri> getImageUri(String userId)
    {
        MutableLiveData<Uri> fileUriLiveData = new MutableLiveData();
        StorageReference ref = getStorageReference().child(FOLDER + userId + IMAGE_TYPE);
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                fileUriLiveData.setValue(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Timber.d("Error loading image");
            }
        });
        return fileUriLiveData;
    }

    public LiveData<Car> getCarDetails(String userId){
        MutableLiveData<Car> carLiveData = new MutableLiveData();
        mCarsReference.child(userId).orderByChild("model").
        addValueEventListener(new ValueEventListener() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("result ", dataSnapshot);
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Car carDetails = dsp.getValue(Car.class);
                    Timber.d("result: %s", carDetails);
                    carLiveData.setValue(carDetails);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Timber.d("result: %s", error);
            }
        });
        return carLiveData;
    }


}
