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
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.utils.LocationHelper;

public class AcceptRejectPassengerDialog extends DialogFragment implements View.OnClickListener {

    public static final String NOTIFICATION_KEY = "NOTIFICATION";
    private Notification mNotification;

    public interface ConfirmationListener {
        void onDriverConfirmation(Boolean isAccepted, Notification notification);
    }

    public static AcceptRejectPassengerDialog getInstance(Notification notification) {
        AcceptRejectPassengerDialog acceptRejectPassengerDialog = new AcceptRejectPassengerDialog();
        Bundle args = new Bundle();
        args.putSerializable(NOTIFICATION_KEY, notification);
        acceptRejectPassengerDialog.setArguments(args);
        return acceptRejectPassengerDialog;
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

        Bundle arguments = getArguments();
        if (arguments != null) {
             mNotification = (Notification) getArguments().getSerializable(NOTIFICATION_KEY);

            TextView driverConfirmationPassengerName
                    = rootView.findViewById(R.id.driver_confirmation_passenger_name);
            driverConfirmationPassengerName.setText(mNotification.getDriveRequest().getPassenger().getFullName());

            TextView driverConfirmationPassengerStartLocation
                    = rootView.findViewById(R.id.driver_confirmation_passenger_start_location);
            driverConfirmationPassengerStartLocation.setText(LocationHelper
                    .getStringFromPosition(mNotification.getDriveRequest().getStartLocation()));

            TextView driverConfirmationPassengerEndLocation
                    = rootView.findViewById(R.id.driver_confirmation_passenger_end_location);
            driverConfirmationPassengerEndLocation
                    .setText(LocationHelper.getStringFromPosition(mNotification.getDriveRequest().getEndLocation()));
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
                mConfirmationListener.onDriverConfirmation(true, mNotification);
                dismiss();
                break;
            }
            case R.id.driver_confirmation_decline_button: {
                mConfirmationListener.onDriverConfirmation(false, mNotification);
                dismiss();
                break;
            }
        }
    }
}
