package com.cybercom.passenger.flows.createridefragment;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybercom.passenger.MainViewModel;
import com.cybercom.passenger.R;

public class CreateDriveFragment extends Fragment {


    private MainViewModel mMainViewModel;
    private TextView mNumberOfPassengers;
    private TextView mStartLocation;
    private ImageView mAddPassengers;
    private ImageView mRemovePassengers;
    private Button mCreateRide;
    private ProgressBar mCreatingDrive;


    public CreateDriveFragment() { }

    public static CreateDriveFragment newInstance() {

        Bundle args = new Bundle();

        CreateDriveFragment fragment = new CreateDriveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() != null){
            mMainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_drive, container, false);

        mStartLocation = view.findViewById(R.id.create_drive_start_location);

        mNumberOfPassengers = view.findViewById(R.id.create_drive_number_of_passengers);
        mAddPassengers = view.findViewById(R.id.create_drive_add_passenger);
        mRemovePassengers = view.findViewById(R.id.create_drive_remove_passenger);
        mCreateRide = view.findViewById(R.id.create_drive_button);
        mCreatingDrive = view.findViewById(R.id.create_drive_progressbar);

        displayNumberOfPassengers();
        displayStartLocation();

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

            }
        });

        return view;
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


}
