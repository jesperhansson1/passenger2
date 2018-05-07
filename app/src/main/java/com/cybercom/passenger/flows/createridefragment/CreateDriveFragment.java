package com.cybercom.passenger.flows.createridefragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.LocationHelper;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.Task;

import timber.log.Timber;

public class CreateDriveFragment extends Fragment {
    private static final String FILTER_COUNTRY = "SE";

    public static final int DELAY_CLOSE_TIME_DATE_PICKER = 2000;
    public static final int DEFAULT_PASSENGERS = 4;
    public static final String EMPTY_STRING = "";
    public static final int DEFAULT_PASSENGER_DRIVE_REQUEST = 1;
    private static final int DEFAULT_PASSENGER_DRIVE = 4;
    public static final int DOWN_ARROW_ROTATION = 180;
    public static final int DIALOG_ANIMATION_DURATION = 300;
    public static final int ARROW_ANIMATION_DURATION = 500;
    public static final int UP_ARROW_ANIMATION = 0;
    public static final int MARGIN = 40;
    private CreateRideFragmentListener mCreateRideDialogListener;
    private boolean mIsCreateDialogUp = true;
    private static final float DEFAULT_SHOW_AND_HIDE_POSITION = 0;

    public interface OnPlaceMarkerIconClickListener {
        void onPlaceMarkerIconClicked();

    }
    public interface  OnFinishedCreatingDriveOrDriveRequest{
        void onFinish();

    }

    public interface CreateRideFragmentListener {
        void onCreateRide(long time, int type, Position startLocation, Position endLocation, int seats);
    }

    private OnPlaceMarkerIconClickListener onPlaceMarkerIconClickListener;
    private OnFinishedCreatingDriveOrDriveRequest onFinishedCreatingDriveOrDriveRequest;
    private int mType;
    private MainViewModel mMainViewModel;
    private TextView mNumberOfPassengers;
    private AutoCompleteTextView mStartLocation, mEndLocation;
    private Button mCreateRide;
    private ProgressBar mCreatingDrive;
    private ImageView mAddPassengers;
    private ImageView mRemovePassengers;
    private RadioGroup mTimeSelection;
    private EditText mShowSelectedTime;
    private long mTimeSelected;
    private FrameLayout mShowAndHide;
    private CardView mCreateDriveDialog;

    private SingleDateAndTimePicker mDateTimePicker;

    protected GeoDataClient mGeoDataClient;

    private LocationAutoCompleteAdapter mAdapter;

    private static final LatLngBounds BOUNDS_SWEDEN = new LatLngBounds(
            new LatLng(MainViewModel.LOWER_LEFT_LATITUDE, MainViewModel.LOWER_LEFT_LONGITUDE),
            new LatLng(MainViewModel.UPPER_RIGHT_LATITUDE, MainViewModel.UPPER_RIGHT_LONGITUDE));

    private static final AutocompleteFilter AUTOCOMPLETE_LOCATION_FILTER =
            new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setCountry(FILTER_COUNTRY)
                    .build();
    private ImageView mPlaceStartLocation;

    private ImageView mPlaceEndLocation;
    private Handler mHandler;

    private Runnable mCloseDateTimePickerRunnable = new Runnable() {
        @Override
        public void run() {
            mDateTimePicker.setVisibility(View.GONE);
        }
    };

    public CreateDriveFragment() {
    }

    public static CreateDriveFragment newInstance() {

        Bundle args = new Bundle();

        CreateDriveFragment fragment = new CreateDriveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mMainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_drive, container, false);

        mCreateDriveDialog = view.findViewById(R.id.create_drive_dialog);
        mHandler = new Handler();

        mTimeSelected = System.currentTimeMillis();

        mStartLocation = view.findViewById(R.id.create_drive_start_location);
        mStartLocation.setOnItemClickListener(mStartLocationAutoCompleteClickListener);
        mEndLocation = view.findViewById(R.id.create_drive_end_location);
        mEndLocation.setOnItemClickListener(mEndLocationAutoCompleteClickListener);

        mNumberOfPassengers = view.findViewById(R.id.create_drive_number_of_passengers);
        mAddPassengers = view.findViewById(R.id.create_drive_add_passenger);
        mRemovePassengers = view.findViewById(R.id.create_drive_remove_passenger);
        mCreateRide = view.findViewById(R.id.create_drive_button);
        mCreatingDrive = view.findViewById(R.id.create_drive_progressbar);

        mTimeSelection = view.findViewById(R.id.create_drive_radio_group_when_selection);
        mTimeSelection.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.create_drive_button_right_now: {
                    mDateTimePicker.setVisibility(View.GONE);
                    mTimeSelected = System.currentTimeMillis();
                    mShowSelectedTime.setVisibility(View.GONE);
                    break;
                }
                case R.id.create_drive_button_time: {
                    mDateTimePicker.setVisibility(View.VISIBLE);
                    break;
                }
            }
        });

        mShowSelectedTime = view.findViewById(R.id.create_drive_time_selected);

        mPlaceStartLocation = view.findViewById(R.id.place_start_location);
        mPlaceEndLocation = view.findViewById(R.id.place_end_location);

        mDateTimePicker = view.findViewById(R.id.create_drive_date_and_time_picker);

        mDateTimePicker.addOnDateChangedListener((dateString, date) -> {
            mHandler.removeCallbacks(mCloseDateTimePickerRunnable);
            Timber.i("Date and time selected: %s", date.getTime());
            mShowSelectedTime.setVisibility(View.VISIBLE);
            mShowSelectedTime.setText(dateString);
            mTimeSelected = date.getTime();
            mHandler.postDelayed(mCloseDateTimePickerRunnable, DELAY_CLOSE_TIME_DATE_PICKER);

        });

        mPlaceStartLocation.setOnClickListener(v -> {
            mMainViewModel.setWhichMarkerToAdd(MainViewModel.PLACE_START_MARKER);
            onPlaceMarkerIconClickListener.onPlaceMarkerIconClicked();
        });

        mPlaceEndLocation.setOnClickListener(v -> {
            mMainViewModel.setWhichMarkerToAdd(MainViewModel.PLACE_END_MARKER);
            onPlaceMarkerIconClickListener.onPlaceMarkerIconClicked();
        });


        if (getActivity() != null) {
            mMainViewModel.getUser().observe(getActivity(), user -> {
                if (user != null) {

                    if (user.getType() == User.TYPE_DRIVER) {
                        setUpDialogForDrive();
                        mType = user.getType();
                    }
                    if (user.getType() == User.TYPE_PASSENGER) {
                        setUpDialogForDriveRequest();
                        mType = user.getType();
                    }
                }
            });
        }

        displayNumberOfPassengers();
        displayStartLocation();
        displayEndLocation();

        if (getActivity() != null) {
            mGeoDataClient = Places.getGeoDataClient(getActivity());
        }

        mAdapter = new LocationAutoCompleteAdapter(getContext(), mGeoDataClient,
                BOUNDS_SWEDEN, AUTOCOMPLETE_LOCATION_FILTER);
        mStartLocation.setAdapter(mAdapter);
        mEndLocation.setAdapter(mAdapter);

        mAddPassengers.setOnClickListener(v -> {
            mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() + 1);
            displayNumberOfPassengers();
        });

        mRemovePassengers.setOnClickListener(v -> {
            mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() - 1);
            displayNumberOfPassengers();
        });

        mCreateRide.setOnClickListener(v -> {

            if (mMainViewModel.getStartMarkerLocation().getValue() != null
                    && mMainViewModel.getEndMarkerLocation().getValue() != null) {

                disableDialog();

                mCreateRideDialogListener.onCreateRide(mTimeSelected, mType, LocationHelper.convertLocationToPosition(
                        mMainViewModel.getStartMarkerLocation().getValue()),
                        LocationHelper.convertLocationToPosition(mMainViewModel.getEndMarkerLocation().getValue()), mMainViewModel.getNumberOfPassengers());
            }

        });

        mShowAndHide = view.findViewById(R.id.create_drive_show_and_hide);
        mShowAndHide.setOnClickListener(v -> {
            if(mIsCreateDialogUp){
                hideCreateDialog();
            }else{
                showCreateDialog();
            }

            mIsCreateDialogUp = !mIsCreateDialogUp;

        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPlaceMarkerIconClickListener
                && context instanceof OnFinishedCreatingDriveOrDriveRequest) {
            onPlaceMarkerIconClickListener = (OnPlaceMarkerIconClickListener) context;
            onFinishedCreatingDriveOrDriveRequest = (OnFinishedCreatingDriveOrDriveRequest) context;
        } else {
            Toast.makeText(context, R.string.must_implement_on_place_icon_click_listener,
                    Toast.LENGTH_SHORT).show();
        }

        if (context instanceof CreateRideFragmentListener) {
            mCreateRideDialogListener = (CreateRideFragmentListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_passenger_notification_listener,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void disableDialog() {
        mCreateRide.setText(null);
        mCreateRide.setEnabled(false);
        mCreatingDrive.setVisibility(View.VISIBLE);
    }

    public void setDefaultValuesToDialog() {

        if (mType == User.TYPE_DRIVER) {
            mCreateRide.setText(R.string.create_ride);
        }
        if (mType == User.TYPE_PASSENGER) {
            mCreateRide.setText(R.string.create_drive_find_ride);
        }

        mStartLocation.setText(EMPTY_STRING, false);
        mEndLocation.setText(EMPTY_STRING, false);
        mCreateRide.setEnabled(true);
        mCreatingDrive.setVisibility(View.GONE);
        mMainViewModel.setNumberOfPassengers(DEFAULT_PASSENGERS);
        mShowSelectedTime.setText(EMPTY_STRING);
        mShowSelectedTime.setVisibility(View.GONE);
        onFinishedCreatingDriveOrDriveRequest.onFinish();

    }

    private void setUpDialogForDrive() {
        mMainViewModel.setNumberOfPassengers(DEFAULT_PASSENGER_DRIVE);
        mCreateRide.setText(R.string.create_ride);
    }

    private void setUpDialogForDriveRequest() {
        mMainViewModel.setNumberOfPassengers(DEFAULT_PASSENGER_DRIVE_REQUEST);
        mCreateRide.setText(R.string.create_drive_find_ride);
    }

    private void displayEndLocation() {
        mMainViewModel.getEndLocationAddress().observe(this, endAddress -> mEndLocation.setText(endAddress, false));
    }

    private void displayStartLocation() {
        mMainViewModel.getStartLocationAddress().observe(this, startAddress -> mStartLocation.setText(startAddress, false));
    }

    private void displayNumberOfPassengers() {
        mNumberOfPassengers.setText(String.valueOf(mMainViewModel.getNumberOfPassengers()));
    }

    public void showCreateDialog(){
        mCreateDriveDialog.animate().translationY(DEFAULT_SHOW_AND_HIDE_POSITION).setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHide.animate().rotation(DOWN_ARROW_ROTATION).setDuration(ARROW_ANIMATION_DURATION);
    }

    public void hideCreateDialog(){
        mStartLocation.clearFocus();
        mCreateDriveDialog.animate()
                .translationY((mCreateDriveDialog.getHeight() + MARGIN) - mShowAndHide.getHeight())
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHide.animate().rotation(UP_ARROW_ANIMATION).setDuration(ARROW_ANIMATION_DURATION);

    }

    private AdapterView.OnItemClickListener mStartLocationAutoCompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction location = mAdapter.getItem(position);
            final String locationId = location != null ? location.getPlaceId() : null;

            mGeoDataClient.getPlaceById(locationId)
                    .addOnCompleteListener((Task<PlaceBufferResponse> task) -> {
                        try {
                            PlaceBufferResponse locations = task.getResult();
                            Place clickedLocation = locations.get(0);

                            if (getActivity() != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity()
                                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,
                                            0);
                                }

                                mMainViewModel.setStartMarkerLocation(LocationHelper
                                        .convertLatLngToLocation(clickedLocation.getLatLng()));

                                locations.release();
                            }

                        } catch (RuntimeRemoteException e) {
                            Timber.e("Couldn't find location. &s", e);
                        }
                    });
        }
    };

    private AdapterView.OnItemClickListener mEndLocationAutoCompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction location = mAdapter.getItem(position);
            final String locationId = location != null ? location.getPlaceId() : null;

            mGeoDataClient.getPlaceById(locationId)
                    .addOnCompleteListener(task -> {
                        try {
                            PlaceBufferResponse locations = task.getResult();
                            Place clickedLocation = locations.get(0);

                            if (getActivity() != null) {
                                InputMethodManager imm = (InputMethodManager)
                                        getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }

                                mMainViewModel.setEndMarkerLocation(LocationHelper
                                        .convertLatLngToLocation(clickedLocation.getLatLng()));

                                locations.release();
                            }

                        } catch (RuntimeRemoteException e) {
                            Timber.e("Couldn't find location. &s", e);
                        }
                    });
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();

        mCreateRideDialogListener = null;
    }

}
