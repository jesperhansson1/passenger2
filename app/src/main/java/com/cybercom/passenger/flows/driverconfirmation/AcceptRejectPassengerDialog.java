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
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;

public class AcceptRejectPassengerDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "DRIVER_CONFIRMATION_DIALOG";

    public static final String DRIVE_KEY = "DRIVE";
    public static final String DRIVE_REQUEST_KEY = "DRIVE_REQUEST";
    private Drive mDrive;
    private DriveRequest mDriveRequest;

    public interface ConfirmationListener {
        void onDriverConfirmation(Boolean isAccepted, Drive drive, DriveRequest driveRequest);
    }

    public static AcceptRejectPassengerDialog getInstance(Drive drive, DriveRequest driveRequest) {
        AcceptRejectPassengerDialog acceptRejectPassengerDialog = new AcceptRejectPassengerDialog();
        Bundle args = new Bundle();
        args.putSerializable(DRIVE_KEY, drive);
        args.putSerializable(DRIVE_REQUEST_KEY, driveRequest);
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
            mDrive = (Drive) getArguments().getSerializable(DRIVE_KEY);
            mDriveRequest = (DriveRequest) getArguments().getSerializable(DRIVE_REQUEST_KEY);
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
                mConfirmationListener.onDriverConfirmation(true, mDrive, mDriveRequest);
                dismiss();
                break;
            }
            case R.id.driver_confirmation_decline_button: {
                mConfirmationListener.onDriverConfirmation(false, mDrive, mDriveRequest);
                dismiss();
                break;
            }
        }
    }
}
