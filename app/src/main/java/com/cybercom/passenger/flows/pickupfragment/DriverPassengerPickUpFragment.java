package com.cybercom.passenger.flows.pickupfragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.PassengerRide;

public class DriverPassengerPickUpFragment extends Fragment implements View.OnClickListener {

    public static final String DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG
            = "DRIVER_PASSENGER_PICK_UP_FRAGMENT";
    public static final String PASSENGER_RIDE_KEY = "PASSENGER";
    private static final String DRIVER_RIDE_KEY = "DRIVER";
    public static final int TIME_BEFORE_PASSENGER_NEEDS_TO_COME_TO_THE_CAR_IN_MILLISECONDS = 120000;
    public static final int COUNT_DOWN_INTERVAL = 1000;


    private FragmentSizeListener mFragmentSizeListener;
    private DriverPassengerPickUpButtonClickListener mDriverPassengerPickUpButtonClickListener;

    private Button mNoShowButton;

    private PassengerRide mPassengerRide;
    private Drive mDrive;

    public interface DriverPassengerPickUpButtonClickListener {
        void onPickUpConfirmed(PassengerRide passengerRide);

        void onPickUpNoShow(PassengerRide passengerRide);

        void onPickUpConfirmed(Drive drive);

        void onPickUpNoShow(Drive drive);
    }

    public static DriverPassengerPickUpFragment newInstance(Drive drive) {
        Bundle args = new Bundle();
        args.putSerializable(DRIVER_RIDE_KEY, drive);
        DriverPassengerPickUpFragment fragment = new DriverPassengerPickUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DriverPassengerPickUpFragment newInstance(PassengerRide passengerRide) {
        Bundle args = new Bundle();
        args.putSerializable(PASSENGER_RIDE_KEY, passengerRide);
        DriverPassengerPickUpFragment fragment = new DriverPassengerPickUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_passenger_pick_up, container, false);

        TextView name = view.findViewById(R.id.fragment_driver_passenger_pick_up_name);
        if (getArguments() != null) {
            if (getArguments().getSerializable(DRIVER_RIDE_KEY) != null) {
                mDrive = (Drive) getArguments().getSerializable(DRIVER_RIDE_KEY);
                if (mDrive != null) {
                    name.setText(mDrive.getDriver().getFullName());
                }
            }

            if (getArguments().getSerializable(PASSENGER_RIDE_KEY) != null) {
                mPassengerRide = (PassengerRide) getArguments().getSerializable(PASSENGER_RIDE_KEY);
                if (mPassengerRide != null) {
                    name.setText(mPassengerRide.getPassenger().getFullName());
                }
            }

        }

        Button mConfirmPickUp
                = view.findViewById(R.id.fragment_driver_passenger_pick_up_confirmation_button);
        mConfirmPickUp.setOnClickListener(this);

        mNoShowButton = view.findViewById(R.id.fragment_driver_passenger_pick_up_no_show_button);
        mNoShowButton.setEnabled(false);
        mNoShowButton.setOnClickListener(this);

        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mFragmentSizeListener.onHeightChanged(view.getHeight());
                        view.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });

        TextView countDown = view.findViewById(R.id.fragment_driver_passenger_pick_up_countdown);
        new CountDownTimer(TIME_BEFORE_PASSENGER_NEEDS_TO_COME_TO_THE_CAR_IN_MILLISECONDS,
                COUNT_DOWN_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                int minutesLeft = (int) (millisUntilFinished / 1000) / 60;
                int secondsLeft = (int) (millisUntilFinished / 1000) % 60;

                if (secondsLeft >= 10) {
                    countDown.setText("0" + minutesLeft + ":" + secondsLeft);
                } else {
                    countDown.setText("0" + minutesLeft + ":0" + secondsLeft);
                }

            }

            @Override
            public void onFinish() {
                mNoShowButton.setEnabled(true);
            }
        }.start();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentSizeListener) {
            mFragmentSizeListener = (FragmentSizeListener) context;
        }
        if (context instanceof DriverPassengerPickUpButtonClickListener) {
            mDriverPassengerPickUpButtonClickListener =
                    (DriverPassengerPickUpButtonClickListener) context;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_driver_passenger_pick_up_confirmation_button: {
                if (mDrive != null) {
                    mDriverPassengerPickUpButtonClickListener.onPickUpConfirmed(mDrive);
                } else {
                    mDriverPassengerPickUpButtonClickListener.onPickUpConfirmed(mPassengerRide);
                }
                mFragmentSizeListener.onHeightChanged(0);
                break;
            }
            case R.id.fragment_driver_passenger_pick_up_no_show_button: {
                if (mDrive != null) {
                    mDriverPassengerPickUpButtonClickListener.onPickUpNoShow(mDrive);
                } else {
                    mDriverPassengerPickUpButtonClickListener.onPickUpNoShow(mPassengerRide);
                }
                mFragmentSizeListener.onHeightChanged(0);
                break;
            }
        }
    }
}
