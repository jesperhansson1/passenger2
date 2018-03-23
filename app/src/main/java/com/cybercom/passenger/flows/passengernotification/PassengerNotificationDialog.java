package com.cybercom.passenger.flows.passengernotification;

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

import timber.log.Timber;

public class PassengerNotificationDialog extends DialogFragment implements View.OnClickListener {

    public interface PassengerNotificationListener {
        void onCancelPressed(Boolean isCancelPressed);
    }

    public static PassengerNotificationDialog getInstance() {
        return new PassengerNotificationDialog();
    }

    private PassengerNotificationListener mPassengerNotificationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_passenger_notification, container,
                false);

        Button cancelButton = rootView.findViewById(R.id.passenger_notification_cancel_button);
        cancelButton.setOnClickListener(this);

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

        if (context instanceof PassengerNotificationListener) {
            mPassengerNotificationListener = (PassengerNotificationListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_passenger_notification_listener,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.passenger_notification_cancel_button: {
                Timber.i("Passenger notification cancel button pressed");
                mPassengerNotificationListener.onCancelPressed(true);
                dismiss();
                break;
            }
        }
    }
}
