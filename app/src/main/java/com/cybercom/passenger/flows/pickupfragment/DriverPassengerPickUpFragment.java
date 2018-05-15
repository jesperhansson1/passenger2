package com.cybercom.passenger.flows.pickupfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybercom.passenger.R;

public class DriverPassengerPickUpFragment extends Fragment {

    public static final String USER_KEY = "USER";

    public static DriverPassengerPickUpFragment newInstance(int type) {

        Bundle args = new Bundle();
        args.putInt(USER_KEY, type);
        DriverPassengerPickUpFragment fragment = new DriverPassengerPickUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_passenger_pick_up, container, false);

        return view;
    }
}
