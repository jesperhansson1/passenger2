package com.cybercom.passenger.flows.createridefragment;

import android.animation.Animator;
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
import android.view.ViewTreeObserver;
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
import com.cybercom.passenger.interfaces.FragmentSizeListener;
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
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class CreateDriveFragment extends Fragment {
    private static final String FILTER_COUNTRY = "SE";
    private static final int DEFAULT_PASSENGER_DRIVE = 4;
    private static final float DEFAULT_SHOW_AND_HIDE_POSITION = 0;

    public static final int DELAY_CLOSE_TIME_DATE_PICKER = 2000;
    public static final int DEFAULT_PASSENGERS = 4;
    public static final String EMPTY_STRING = "";
    public static final int DEFAULT_PASSENGER_DRIVE_REQUEST = 1;
    public static final int DOWN_ARROW_ROTATION = 180;
    public static final int DIALOG_ANIMATION_DURATION = 150;
    public static final int ARROW_ANIMATION_DURATION = 300;
    public static final int UP_ARROW_ANIMATION = 0;
    public static final int MARGIN = 40;

    private CreateRideFragmentListener mCreateRideDialogListener;
    private boolean mIsCreateDialogUp = true;
    private boolean mIsOtherFragmentUp = false;

    public interface OnPlaceMarkerIconClickListener {
        void onPlaceMarkerIconClicked();
    }

    public interface OnFinishedCreatingDriveOrDriveRequest {
        void onFinish();
    }

    public interface CreateRideFragmentListener {
        void onCreateRide(long time, int type, Position startLocation, Position endLocation,
                          int seats);
    }

    private OnPlaceMarkerIconClickListener mOnPlaceMarkerIconClickListener;
    private OnFinishedCreatingDriveOrDriveRequest mOnFinishedCreatingDriveOrDriveRequest;
    private FragmentSizeListener mFragmentSizeListener;
    private int mType;
    private MainViewModel mMainViewModel;
    private TextView mNumberOfPassengersTitle;
    private TextView mNumberOfPassengers;
    private AutoCompleteTextView mStartLocation, mEndLocation;
    private Button mButtonCreateRide;
    private ProgressBar mCreatingDrive;
    private EditText mShowSelectedTime;
    private long mTimeSelected;
    private FrameLayout mShowAndHide;
    private CardView mCreateDriveDialog;

    private SingleDateAndTimePicker mDateTimePicker;

    private GeoDataClient mGeoDataClient;

    private LocationAutoCompleteAdapter mAdapter;

    private static final LatLngBounds BOUNDS_SWEDEN = new LatLngBounds(
            new LatLng(MainViewModel.LOWER_LEFT_LATITUDE, MainViewModel.LOWER_LEFT_LONGITUDE),
            new LatLng(MainViewModel.UPPER_RIGHT_LATITUDE, MainViewModel.UPPER_RIGHT_LONGITUDE));

    private static final AutocompleteFilter AUTOCOMPLETE_LOCATION_FILTER =
            new AutocompleteFilter.Builder()
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

        mCreateDriveDialog.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mFragmentSizeListener != null) {
                        mFragmentSizeListener.onHeightChanged(mCreateDriveDialog.getHeight() +
                                MARGIN);
                    }
                    mCreateDriveDialog.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

        mHandler = new Handler();

        mTimeSelected = System.currentTimeMillis();

        mStartLocation = view.findViewById(R.id.create_drive_start_location);
        mStartLocation.setOnItemClickListener(mStartLocationAutoCompleteClickListener);
        mEndLocation = view.findViewById(R.id.create_drive_end_location);
        mEndLocation.setOnItemClickListener(mEndLocationAutoCompleteClickListener);

        mNumberOfPassengersTitle =
                view.findViewById(R.id.textview_createdrivefragment_nbrpassengerstitle);
        mNumberOfPassengers = view.findViewById(R.id.create_drive_number_of_passengers);
        ImageView addPassengers = view.findViewById(R.id.create_drive_add_passenger);

        ImageView removePassengers = view.findViewById(R.id.create_drive_remove_passenger);
        mButtonCreateRide = view.findViewById(R.id.create_drive_button);
        mCreatingDrive = view.findViewById(R.id.create_drive_progressbar);

        RadioGroup timeSelection = view.findViewById(R.id.create_drive_radio_group_when_selection);
        timeSelection.setOnCheckedChangeListener((group, checkedId) -> {
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
        Picasso.with(getActivity()).load(R.drawable.map_marker_start).into(mPlaceStartLocation);
        mPlaceEndLocation = view.findViewById(R.id.place_end_location);
        Picasso.with(getActivity()).load(R.drawable.map_marker_end).into(mPlaceEndLocation);

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
            mOnPlaceMarkerIconClickListener.onPlaceMarkerIconClicked();
        });

        mPlaceEndLocation.setOnClickListener(v -> {
            mMainViewModel.setWhichMarkerToAdd(MainViewModel.PLACE_END_MARKER);
            mOnPlaceMarkerIconClickListener.onPlaceMarkerIconClicked();
        });

        if (getActivity() != null) {
            mMainViewModel.getUser().observe(getActivity(), user -> {
                if (user != null) {
                    mType = user.getType();
                    if (mType == User.TYPE_DRIVER) {
                        setUpDialogForDrive();
                    } else {
                        setUpDialogForDriveRequest();
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

        addPassengers.setOnClickListener(v -> {
            mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() + 1);
            displayNumberOfPassengers();
        });

        removePassengers.setOnClickListener(v -> {
            mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() - 1);
            displayNumberOfPassengers();
        });

        mButtonCreateRide.setOnClickListener(v -> {

            if (mMainViewModel.getStartMarkerLocation().getValue() != null
                    && mMainViewModel.getEndMarkerLocation().getValue() != null) {

                mCreateRideDialogListener.onCreateRide(
                        mTimeSelected, mType, LocationHelper.convertLocationToPosition(
                                mMainViewModel.getStartMarkerLocation().getValue()),
                        LocationHelper.convertLocationToPosition(mMainViewModel
                                .getEndMarkerLocation().getValue()),
                        mMainViewModel.getNumberOfPassengers());
            }

        });

        mShowAndHide = view.findViewById(R.id.create_drive_show_and_hide);
        mShowAndHide.setOnClickListener(v -> {
            if (!mIsOtherFragmentUp) {
                if (mIsCreateDialogUp) {
                    hideCreateDialog();
                } else {
                    showCreateDialog();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPlaceMarkerIconClickListener
                && context instanceof OnFinishedCreatingDriveOrDriveRequest) {
            mOnPlaceMarkerIconClickListener = (OnPlaceMarkerIconClickListener) context;
            mOnFinishedCreatingDriveOrDriveRequest = (OnFinishedCreatingDriveOrDriveRequest) context;
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

        if (context instanceof FragmentSizeListener) {
            mFragmentSizeListener = (FragmentSizeListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_fragment_is_visible_listener,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setDefaultValuesToDialog() {

        if (mType == User.TYPE_DRIVER) {
            mButtonCreateRide.setText(R.string.create_ride);
        }
        if (mType == User.TYPE_PASSENGER) {
            mButtonCreateRide.setText(R.string.create_drive_find_ride);
        }

        mStartLocation.setText(EMPTY_STRING, false);
        mEndLocation.setText(EMPTY_STRING, false);
        mButtonCreateRide.setEnabled(true);
        mCreatingDrive.setVisibility(View.GONE);
        mMainViewModel.setNumberOfPassengers(DEFAULT_PASSENGERS);
        mShowSelectedTime.setText(EMPTY_STRING);
        mShowSelectedTime.setVisibility(View.GONE);
    }

    private void setUpDialogForDrive() {
        mMainViewModel.setNumberOfPassengers(DEFAULT_PASSENGER_DRIVE);
        displayNumberOfPassengers();
        mButtonCreateRide.setText(R.string.create_ride);
        mNumberOfPassengersTitle.setText(R.string.create_drive_available_seats);
    }

    private void setUpDialogForDriveRequest() {
        mMainViewModel.setNumberOfPassengers(DEFAULT_PASSENGER_DRIVE_REQUEST);
        displayNumberOfPassengers();
        mButtonCreateRide.setText(R.string.create_drive_find_ride);
        mNumberOfPassengersTitle.setText(R.string.create_drive_passengers_title);
    }

    private void displayEndLocation() {
        mMainViewModel.getEndLocationAddress()
                .observe(this, endAddress -> mEndLocation.setText(endAddress, false));
    }

    private void displayStartLocation() {
        mMainViewModel.getStartLocationAddress()
                .observe(this, startAddress -> mStartLocation.setText(startAddress, false));
    }

    private void displayNumberOfPassengers() {
        mNumberOfPassengers.setText(String.valueOf(mMainViewModel.getNumberOfPassengers()));
    }

    public void showCreateDialog() {
        if (mIsCreateDialogUp) {
            return;
        }

        mCreateDriveDialog.animate().translationY(DEFAULT_SHOW_AND_HIDE_POSITION)
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHide.animate().rotation(DOWN_ARROW_ROTATION)
                .setDuration(ARROW_ANIMATION_DURATION);
        if (mFragmentSizeListener != null) {
            mFragmentSizeListener
                    .onHeightChanged(mCreateDriveDialog.getHeight() + MARGIN);
        }
        mIsCreateDialogUp = true;

    }

    public void hideCreateDialog() {
        if (!mIsCreateDialogUp) {
            return;
        }
        mCreateDriveDialog.animate()
                .translationY((mCreateDriveDialog.getHeight() + MARGIN) - mShowAndHide.getHeight())
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHide.animate().rotation(UP_ARROW_ANIMATION).setDuration(ARROW_ANIMATION_DURATION);
        if (mFragmentSizeListener != null) {
            mFragmentSizeListener.onHeightChanged(mShowAndHide.getHeight());
        }
        mIsCreateDialogUp = false;
    }

    public void hideCreateDialogCompletely() {
        if (!mIsCreateDialogUp) {
            return;
        }
        mCreateDriveDialog.animate()
                .translationY(mCreateDriveDialog.getHeight())
                .alpha(0)
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHide.animate().rotation(UP_ARROW_ANIMATION).setDuration(ARROW_ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(mOnFinishedCreatingDriveOrDriveRequest != null) {
                            mOnFinishedCreatingDriveOrDriveRequest.onFinish();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

        if (mFragmentSizeListener != null) {
            mFragmentSizeListener.onHeightChanged(0);
        }
    }

    public void setIsOtherFragmentUp(boolean isOtherFragmentUp) {
        mIsOtherFragmentUp = isOtherFragmentUp;
    }

    private AdapterView.OnItemClickListener mStartLocationAutoCompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @SuppressLint({"RestrictedApi", "TimberArgCount"})
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
        @SuppressLint({"RestrictedApi", "TimberArgCount"})
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
                                InputMethodManager imm = (InputMethodManager) getActivity()
                                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
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
        mOnFinishedCreatingDriveOrDriveRequest = null;
        mFragmentSizeListener = null;
        mOnPlaceMarkerIconClickListener = null;
    }

}
