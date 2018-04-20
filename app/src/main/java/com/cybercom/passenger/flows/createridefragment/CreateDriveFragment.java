package com.cybercom.passenger.flows.createridefragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.ToastHelper;
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

import timber.log.Timber;

public class CreateDriveFragment extends Fragment {

    private MainViewModel mMainViewModel;
    private TextView mNumberOfPassengers;
    private AutoCompleteTextView mStartLocation, mEndLocation;
    private Button mCreateRide;
    private ProgressBar mCreatingDrive;
    private ImageView mAddPassengers;
    private ImageView mRemovePassengers;

    protected GeoDataClient mGeoDataClient;

    private LocationAutoCompleteAdapter mAdapter;


    private static final LatLngBounds BOUNDS_SWEDEN = new LatLngBounds(
            new LatLng(MainViewModel.LOWER_LEFT_LATITUDE, MainViewModel.LOWER_LEFT_LONGITUDE),
            new LatLng(MainViewModel.UPPER_RIGHT_LATITUDE, MainViewModel.UPPER_RIGHT_LONGITUDE));

    private static final AutocompleteFilter AUTOCOMPLETE_LOCATION_FILTER =
            new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setCountry("SE")
                    .build();

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

        mStartLocation = view.findViewById(R.id.create_drive_start_location);
        mStartLocation.setOnItemClickListener(mStartLocationAutoCompleteClickListener);
        mEndLocation = view.findViewById(R.id.create_drive_end_location);
        mEndLocation.setOnItemClickListener(mEndLocationAutoCompleteClickListener);

        mNumberOfPassengers = view.findViewById(R.id.create_drive_number_of_passengers);
        mAddPassengers = view.findViewById(R.id.create_drive_add_passenger);
        mRemovePassengers = view.findViewById(R.id.create_drive_remove_passenger);
        mCreateRide = view.findViewById(R.id.create_drive_button);
        mCreatingDrive = view.findViewById(R.id.create_drive_progressbar);

        displayNumberOfPassengers();
        displayStartLocation();
        displayEndLocation();

        mGeoDataClient = Places.getGeoDataClient(getActivity());
        mAdapter = new LocationAutoCompleteAdapter(getContext(), mGeoDataClient, BOUNDS_SWEDEN, AUTOCOMPLETE_LOCATION_FILTER);
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

        // mStartLocation.addTextChangedListener(mStartLocationListener);
       // mEndLocation.addTextChangedListener(mEndLocationListener);

        mCreateRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateRide.setText(null);
                mCreateRide.setEnabled(false);
                mCreatingDrive.setVisibility(View.VISIBLE);

                if (mMainViewModel.getStartMarkerLocation().getValue() != null
                        && mMainViewModel.getEndMarkerLocation().getValue() != null)
                    mMainViewModel.createDrive(System.currentTimeMillis(),
                            LocationHelper.convertLocationToPosition(mMainViewModel
                                    .getStartMarkerLocation().getValue()),
                            LocationHelper.convertLocationToPosition(mMainViewModel
                                    .getEndMarkerLocation().getValue()),
                            mMainViewModel.getNumberOfPassengers())
                            .observe(CreateDriveFragment.this, new Observer<Drive>() {
                                @Override
                                public void onChanged(@Nullable Drive drive) {
                                    ToastHelper.makeToast("Drive is created", getActivity()).show();
                                }
                            });


            }
        });

        return view;
    }

    private void displayEndLocation() {
        mMainViewModel.getEndLocationAddress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String endAddress) {
                mEndLocation.setText(endAddress);
            }
        });
    }

    private void displayStartLocation() {
        mMainViewModel.getStartLocationAddress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String startAddress) {
                mStartLocation.setText(startAddress);
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


            System.out.println("StartLocation");
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
}
