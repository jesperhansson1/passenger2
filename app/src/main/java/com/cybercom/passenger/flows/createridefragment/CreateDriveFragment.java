package com.cybercom.passenger.flows.createridefragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.ToastHelper;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;

import timber.log.Timber;

public class CreateDriveFragment extends Fragment {

    private static final String FILTER_COUNTRY = "SE";
    public static final int DELAY_CLOSE_TIME_DATE_PICKER = 2000;
    public static final int DEFAULT_PASSENGERS = 4;
    public static final String EMPTY_STRING = "";

    public interface OnPlaceMarkerIconClickListener {
        void onPlaceMarkerIconClicked();
    }

    private OnPlaceMarkerIconClickListener onPlaceMarkerIconClickListener;
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
        mTimeSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.create_drive_button_right_now: {
                        mDateTimePicker.setVisibility(View.GONE);
                        mTimeSelected = System.currentTimeMillis();
                        break;
                    }
                    case R.id.create_drive_button_time: {
                        mDateTimePicker.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });

        mShowSelectedTime = view.findViewById(R.id.create_drive_time_selected);

        mPlaceStartLocation = view.findViewById(R.id.place_start_location);
        mPlaceEndLocation = view.findViewById(R.id.place_end_location);

        mDateTimePicker = view.findViewById(R.id.create_drive_date_and_time_picker);

        mDateTimePicker.addOnDateChangedListener(new SingleDateAndTimePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(String dateString, Date date) {
                mHandler.removeCallbacks(mCloseDateTimePickerRunnable);
                Timber.i("Date and time selected: %s", date.getTime());
                mShowSelectedTime.setVisibility(View.VISIBLE);
                mShowSelectedTime.setText(dateString);
                mTimeSelected = date.getTime();
                mHandler.postDelayed(mCloseDateTimePickerRunnable, DELAY_CLOSE_TIME_DATE_PICKER);

            }
        });

        mPlaceStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.setWhichMarkerToAdd(MainViewModel.PLACE_START_MARKER);
                onPlaceMarkerIconClickListener.onPlaceMarkerIconClicked();
            }
        });

        mPlaceEndLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.setWhichMarkerToAdd(MainViewModel.PLACE_END_MARKER);
                onPlaceMarkerIconClickListener.onPlaceMarkerIconClicked();
            }
        });


        if (getActivity() != null) {
            mMainViewModel.getUser().observe(getActivity(), new Observer<User>() {
                @Override
                public void onChanged(@Nullable User user) {
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

        mAddPassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() + 1);
                displayNumberOfPassengers();
            }
        });

        mRemovePassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() - 1);
                displayNumberOfPassengers();
            }
        });


        mCreateRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateRide.setText(null);
                mCreateRide.setEnabled(false);
                mCreatingDrive.setVisibility(View.VISIBLE);

                if (mMainViewModel.getStartMarkerLocation().getValue() != null
                        && mMainViewModel.getEndMarkerLocation().getValue() != null)

                    if (mType == User.TYPE_DRIVER) {
                        mMainViewModel.createDrive(mTimeSelected,
                                LocationHelper.convertLocationToPosition(mMainViewModel
                                        .getStartMarkerLocation().getValue()),
                                LocationHelper.convertLocationToPosition(mMainViewModel
                                        .getEndMarkerLocation().getValue()),
                                mMainViewModel.getNumberOfPassengers())
                                .observe(CreateDriveFragment.this, new Observer<Drive>() {
                                    @Override
                                    public void onChanged(@Nullable Drive drive) {
                                        if (drive != null) {
                                            Timber.i("Drive is created: %s",
                                                    drive.toString());
                                        }
                                        ToastHelper.makeToast("Drive is created",
                                                getActivity()).show();
                                        setDefaultValuesToDialog();
                                    }
                                });
                    }

                if (mType == User.TYPE_PASSENGER) {
                    mMainViewModel.createDriveRequest(mTimeSelected,
                            LocationHelper.convertLocationToPosition(mMainViewModel
                                    .getStartMarkerLocation().getValue()),
                            LocationHelper.convertLocationToPosition(mMainViewModel
                                    .getEndMarkerLocation().getValue()),
                            mMainViewModel.getNumberOfPassengers())
                            .observe(CreateDriveFragment.this, new Observer<DriveRequest>() {
                                @Override
                                public void onChanged(@Nullable DriveRequest driveRequest) {
                                    if (driveRequest != null) {
                                        Timber.i("Driverequest is created: %s",
                                                driveRequest.toString());
                                        matchDriveRequest(driveRequest);
                                    }
                                    ToastHelper.makeToast("Driverequest is created",
                                            getActivity()).show();
                                    setDefaultValuesToDialog();
                                }
                            });
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPlaceMarkerIconClickListener) {
            onPlaceMarkerIconClickListener = (OnPlaceMarkerIconClickListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_on_place_icon_click_listener,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setDefaultValuesToDialog() {

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

    }

    private void setUpDialogForDrive() {
        mCreateRide.setText(R.string.create_ride);
    }

    private void setUpDialogForDriveRequest() {
        mCreateRide.setText(R.string.create_drive_find_ride);
    }

    private void displayEndLocation() {
        mMainViewModel.getEndLocationAddress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String endAddress) {
                mEndLocation.setText(endAddress, false);
            }
        });
    }

    private void displayStartLocation() {
        mMainViewModel.getStartLocationAddress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String startAddress) {
                mStartLocation.setText(startAddress, false);
            }
        });
    }

    private void displayNumberOfPassengers() {
        mNumberOfPassengers.setText(String.valueOf(mMainViewModel.getNumberOfPassengers()));
    }

    private AdapterView.OnItemClickListener mStartLocationAutoCompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction location = mAdapter.getItem(position);
            final String locationId = location != null ? location.getPlaceId() : null;

            mGeoDataClient.getPlaceById(locationId)
                    .addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
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
                                            .convertPositionToLocation(clickedLocation.getLatLng()));

                                    locations.release();
                                }

                            } catch (RuntimeRemoteException e) {
                                Timber.e("Couldn't find location. &s", e);
                            }
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
                    .addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            try {
                                PlaceBufferResponse locations = task.getResult();
                                Place clickedLocation = locations.get(0);

                                if (getActivity() != null) {
                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    if (imm != null) {
                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    }

                                    mMainViewModel.setEndMarkerLocation(LocationHelper
                                            .convertPositionToLocation(clickedLocation.getLatLng()));

                                    locations.release();
                                }

                            } catch (RuntimeRemoteException e) {
                                Timber.e("Couldn't find location. &s", e);
                            }
                        }
                    });
        }
    };

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
                Toast.makeText(getActivity(), R.string.main_activity_match_not_found_message,
                        Toast.LENGTH_LONG).show();
                findMatch.removeObserver(matchObserver);
                timerObserver.removeObservers(lifecycleOwner);
            }
        });
    }
}
