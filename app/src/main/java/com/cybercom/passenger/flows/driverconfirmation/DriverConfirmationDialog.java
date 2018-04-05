package com.cybercom.passenger.flows.driverconfirmation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;

public class DriverConfirmationDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "DRIVER_CONFIRMATION_DIALOG";
    public static final String PASSENGER_NAME = "PASSENGER_NAME";
    public static final String PASSENGER_START_LOCATION = "PASSENGER_START_LOCATION";
    public static final String PASSENGER_END_LOCATION = "PASSENGER_END_LOCATION";

    public interface ConfirmationListener {
        void onDriverConfirmation(Boolean isAccepted);
    }

    public static DriverConfirmationDialog getInstance(String name, String startLocation
            , String endLocation) {
        DriverConfirmationDialog driverConfirmationDialog = new DriverConfirmationDialog();
        Bundle args = new Bundle();
        args.putString(PASSENGER_NAME, name);
        args.putString(PASSENGER_START_LOCATION, startLocation);
        args.putString(PASSENGER_END_LOCATION, endLocation);
        driverConfirmationDialog.setArguments(args);
        return driverConfirmationDialog;
    }

    private ConfirmationListener mConfirmationListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_driver_confirmation, container,
                false);

        Button acceptButton = rootView.findViewById(R.id.driver_confirmation_accept_button);
        acceptButton.setOnClickListener(this);

        Button declineButton = rootView.findViewById(R.id.driver_confirmation_decline_button);
        declineButton.setOnClickListener(this);

        TextView driverConfirmationPassengerName
                = rootView.findViewById(R.id.driver_confirmation_passenger_name);
        TextView driverConfirmationPassengerStartLocation
                = rootView.findViewById(R.id.driver_confirmation_start_position);
        TextView driverConfirmationPassengerEndLocation
                = rootView.findViewById(R.id.driver_confirmation_end_position);

        if (getArguments() != null) {
            driverConfirmationPassengerName
                    .setText(getArguments().getString(PASSENGER_NAME));
            driverConfirmationPassengerStartLocation
                    .setText(getArguments().getString(PASSENGER_START_LOCATION));
            driverConfirmationPassengerEndLocation
                    .setText(getArguments().getString(PASSENGER_END_LOCATION));
        }


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            dismiss();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ConfirmationListener) {
            mConfirmationListener = (ConfirmationListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_confirmation_listener,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.driver_confirmation_accept_button: {
                mConfirmationListener.onDriverConfirmation(true);
                dismiss();
                break;
            }
            case R.id.driver_confirmation_decline_button: {
                mConfirmationListener.onDriverConfirmation(false);
                dismiss();
                break;
            }
        }
    }
}
