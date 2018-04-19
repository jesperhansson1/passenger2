package com.cybercom.passenger.flows.createridefragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.ToastHelper;

public class CreateDriveFragment extends Fragment {

    private MainViewModel mMainViewModel;
    private TextView mNumberOfPassengers;
    private TextView mStartLocation;
    private TextView mEndLocation;
    private Button mCreateRide;
    private ProgressBar mCreatingDrive;
    private boolean isInsertedByApp;
    private ImageView mAddPassengers;
    private ImageView mRemovePassengers;

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
        mEndLocation = view.findViewById(R.id.create_drive_end_location);

        mNumberOfPassengers = view.findViewById(R.id.create_drive_number_of_passengers);
        mAddPassengers = view.findViewById(R.id.create_drive_add_passenger);
        mRemovePassengers = view.findViewById(R.id.create_drive_remove_passenger);
        mCreateRide = view.findViewById(R.id.create_drive_button);
        mCreatingDrive = view.findViewById(R.id.create_drive_progressbar);

        displayNumberOfPassengers();
        displayStartLocation();
        displayEndLocation();

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

        mStartLocation.addTextChangedListener(mStartLocationListener);
        mEndLocation.addTextChangedListener(mEndLocationListener);

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
                isInsertedByApp = true;
                mEndLocation.setText(endAddress);
                isInsertedByApp = false;
            }
        });
    }

    private void displayStartLocation() {
        mMainViewModel.getStartLocationAddress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String startAddress) {
                isInsertedByApp = true;
                mStartLocation.setText(startAddress);
                isInsertedByApp = false;
            }
        });
    }

    private void displayNumberOfPassengers() {
        mNumberOfPassengers.setText(String.valueOf(mMainViewModel.getNumberOfPassengers()));
    }

    private final TextWatcher mStartLocationListener = new TextWatcher() {
        final Handler handler = new Handler();
        Runnable runnable;

        public void onTextChanged(final CharSequence s, int start, final int before, int count) {
            if (!isInsertedByApp) {
                handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void afterTextChanged(final Editable address) {

            if (!isInsertedByApp) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        mMainViewModel.setStartLocationAddress(address.toString());
                    }
                };
                handler.postDelayed(runnable, 2000);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    };

    private final TextWatcher mEndLocationListener = new TextWatcher() {
        final Handler handler = new Handler();
        Runnable runnable;

        public void onTextChanged(final CharSequence s, int start, final int before, int count) {
            if (!isInsertedByApp) {
                handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void afterTextChanged(final Editable address) {

            if (!isInsertedByApp) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        mMainViewModel.setEndLocationAddress(address.toString());
                    }
                };
                handler.postDelayed(runnable, 2000);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    };


}
