package com.cybercom.passenger;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateDriveFragment extends Fragment {


    MainViewModel mMainViewModel;
    private TextView mNumberOfPassengers;

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
        mNumberOfPassengers = view.findViewById(R.id.create_drive_number_of_passengers);

        ImageView addPassengers = view.findViewById(R.id.create_drive_add_passenger);
        ImageView removePassengers = view.findViewById(R.id.create_drive_remove_passenger);
        displayNumberOfPassengers();



        addPassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() + 1);
                displayNumberOfPassengers();
            }
        });

        removePassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.setNumberOfPassengers(mMainViewModel.getNumberOfPassengers() - 1);
                displayNumberOfPassengers();
            }
        });

        return view;
    }

    private void displayNumberOfPassengers() {
        mNumberOfPassengers.setText(String.valueOf(mMainViewModel.getNumberOfPassengers()));
    }


}
