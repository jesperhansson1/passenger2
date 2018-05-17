package com.cybercom.passenger.flows.pickupfragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.PassengerRide;

public class DriverPassengerPickUpFragment extends Fragment {

    public static final String PASSENGER_RIDE_KEY = "PASSENGER";
    private FragmentSizeListener mFragmentSizeListener;

    public static DriverPassengerPickUpFragment newInstance(PassengerRide passengerRide) {
        Bundle args = new Bundle();
        args.putSerializable(PASSENGER_RIDE_KEY, passengerRide);
        DriverPassengerPickUpFragment fragment = new DriverPassengerPickUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_passenger_pick_up, container, false);

        TextView name = view.findViewById(R.id.fragment_driver_passenger_pick_up_name);
        PassengerRide pr = (PassengerRide) getArguments().getSerializable(PASSENGER_RIDE_KEY);

        name.setText(pr.getPassegnerId());
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mFragmentSizeListener.onHeightChanged(view.getHeight());
                        view.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentSizeListener) {
            mFragmentSizeListener = (FragmentSizeListener) context;
        }
    }
}
