package com.cybercom.passenger.flows.passengernotification;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.model.Notification;

import timber.log.Timber;

public class PassengerNotificationDialog extends DialogFragment implements View.OnClickListener {

    private static final String NOTIFICATION_KEY = "NOTIFICATION";
    public static final String TAG = "PASSENGER_NOTIFICATION_DIALOG";
    private Notification mNotification;
    private MainViewModel mMainViewModel;
    private Observer<Integer> mETAObserver;
    private LiveData<Integer> mGetETA;

    public interface PassengerNotificationListener {
        void onCancelDrive(Notification notification);
    }

    public static PassengerNotificationDialog getInstance(Notification notification) {
        PassengerNotificationDialog passengerNotificationDialog = new PassengerNotificationDialog();
        Bundle args = new Bundle();
        args.putSerializable(NOTIFICATION_KEY, notification);
        passengerNotificationDialog.setArguments(args);
        return passengerNotificationDialog;
    }

    private PassengerNotificationListener mPassengerNotificationListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mMainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_passenger_notification, container,
                false);

        Button cancelButton = rootView.findViewById(R.id.passenger_notification_cancel_button);
        cancelButton.setOnClickListener(this);

        if (getActivity() != null) {

            mETAObserver = integer -> {
                if (integer != null) {
                    if(isAdded()){
                        rootView.findViewById(R.id.passenger_notification_eta_progressbar).setVisibility(View.GONE);
                        ((TextView) rootView.findViewById(R.id.passenger_notification_eta_text)).setText(
                            getString(R.string.eta_minutes, integer));
                    }
                }
            };

            mGetETA = mMainViewModel.getETA();
            mGetETA.observe(getActivity(), mETAObserver);

            if (getArguments() != null) {
                mNotification = (Notification) getArguments().getSerializable(NOTIFICATION_KEY);

                TextView passengerNotificationName
                        = rootView.findViewById(R.id.passenger_notification_name);
                passengerNotificationName.setText(mNotification.getDrive().getDriver().getFullName());
            }
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

            getDialog().setCanceledOnTouchOutside(false);

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
    public void onDetach() {
        super.onDetach();
        mGetETA.removeObserver(mETAObserver);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.passenger_notification_cancel_button: {
                Timber.i("Passenger notification cancel button pressed");
                mPassengerNotificationListener.onCancelDrive(mNotification);
                dismiss();
                break;
            }
        }
    }
}
