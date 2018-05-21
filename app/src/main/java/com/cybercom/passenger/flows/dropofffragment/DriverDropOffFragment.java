package com.cybercom.passenger.flows.dropofffragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.PassengerRide;

public class DriverDropOffFragment extends Fragment {

    public static final String PASSENGER_RIDE_KEY = "PASSENGER";

    public static DriverDropOffFragment newInstance() {
        Bundle args = new Bundle();
        DriverDropOffFragment fragment = new DriverDropOffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DriverDropOffFragment newInstance(PassengerRide passengerRide) {
        Bundle args = new Bundle();
        args.putSerializable(PASSENGER_RIDE_KEY, passengerRide);
        DriverDropOffFragment fragment = new DriverDropOffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_drop_off, container, false);

        if (getArguments() != null) {
            PassengerRide passengerRide = (PassengerRide) getArguments()
                    .getSerializable(PASSENGER_RIDE_KEY);

            if (passengerRide != null) {
                TextView driverDropOffTitle = view.findViewById(R.id.fragment_driver_drop_off_title);
                String passengerFirstName = passengerRide.getPassenger().getFullName().split(" ")[0];
                driverDropOffTitle.setText(getString(R.string.fragment_driver_drop_off_title,
                        passengerFirstName));
                TextView passengerName = view.findViewById(R.id.fragment_driver_drop_off_passenger_name);
                passengerName.setText(passengerRide.getPassenger().getFullName());
            }

        }

        return view;
    }
}
