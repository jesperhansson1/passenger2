package com.cybercom.passenger.flows.driverconfirmation;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.repository.PassengerRepository;
import com.cybercom.passenger.utils.LocationHelper;
import com.cybercom.passenger.utils.RoundCornersTransformation;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.FARE_PER_MILE;
import static com.cybercom.passenger.utils.RoundCornersTransformation.RADIUS;

public class AcceptRejectPassengerDialog extends DialogFragment implements View.OnClickListener {

    private static final String NOTIFICATION_KEY = "NOTIFICATION";
    public static final String TAG = "ACCEPT_REJECT_PASSENGER_DIALOG";
    private Notification mNotification;

    public interface ConfirmationListener {
        void onDriverConfirmation(boolean isAccepted, Notification notification);
    }

    public static AcceptRejectPassengerDialog getInstance(Notification notification) {
        AcceptRejectPassengerDialog acceptRejectPassengerDialog = new AcceptRejectPassengerDialog();
        Bundle args = new Bundle();
        args.putSerializable(NOTIFICATION_KEY, notification);
        acceptRejectPassengerDialog.setArguments(args);
        return acceptRejectPassengerDialog;
    }

    private ConfirmationListener mConfirmationListener;

    @SuppressLint("TimberArgCount")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_driver_confirmation, container,
                false);

        Button acceptButton = rootView.findViewById(R.id.driver_confirmation_accept_button);
        acceptButton.setOnClickListener(this);

        setCancelable(false);
        Button declineButton = rootView.findViewById(R.id.driver_confirmation_decline_button);
        declineButton.setOnClickListener(this);

        ImageView leafImageView = rootView.findViewById(R.id.driver_confirmation_leaf);
        Picasso.with(getActivity()).load(R.drawable.leaf).into(leafImageView);

        ImageView startLocationImageView = rootView.findViewById(R.id.driver_confirmation_start_location_icon);
        Picasso.with(getActivity()).load(R.drawable.map_marker_start).into(startLocationImageView);

        ImageView endLocationImageView = rootView.findViewById(R.id.driver_confirmation_end_icon);
        Picasso.with(getActivity()).load(R.drawable.map_marker_end).into(endLocationImageView);

        Bundle arguments = getArguments();
        if (arguments != null) {
             mNotification = (Notification) getArguments().getSerializable(NOTIFICATION_KEY);

            TextView driverConfirmationPassengerName
                    = rootView.findViewById(R.id.driver_confirmation_passenger_name);
            driverConfirmationPassengerName.setText(mNotification.getDriveRequest().getPassenger().getFullName());

            TextView driverConfirmationPassengerStartLocation
                    = rootView.findViewById(R.id.driver_confirmation_passenger_start_location);
            driverConfirmationPassengerStartLocation.setText(getAddressFromLocation(
                    LocationHelper.convertPositionToLocation(mNotification.getDriveRequest()
                            .getStartLocation())));

            TextView driverConfirmationPassengerEndLocation
                    = rootView.findViewById(R.id.driver_confirmation_passenger_end_location);
            driverConfirmationPassengerEndLocation
                    .setText(getAddressFromLocation(LocationHelper
                            .convertPositionToLocation(mNotification.getDriveRequest().getEndLocation())));

            TextView driverConfirmationDriveCost = rootView.findViewById(R.id.driver_confirmation_drive_cost);
            driverConfirmationDriveCost.setText(String.valueOf((mNotification.getDriveRequest().getDistance()) * FARE_PER_MILE));
        }

        ImageView passengerImageView = rootView.findViewById(R.id.driver_confirmation_passenger_thumbnail);

        LiveData<Uri> imageUri = PassengerRepository.getInstance().getImageUri(mNotification.getDriveRequest().getPassenger().getUserId());
        imageUri.observe(this,uri -> {
            Timber.d("image uri ", uri.toString());
            try {
                URL url = new URL(uri.toString());
                Timber.d("image url ", url);
                Picasso.with(getContext()).load(String.valueOf(url)).fit()
                        .centerCrop().transform(new RoundCornersTransformation(RADIUS,
                        0, true, false)).into(passengerImageView);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

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

    private String getAddressFromLocation(Location location) {
        List<Address> addresses;
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if(addresses.size() != 0){
                Timber.i(addresses.get(0).toString());
                return addresses.get(0).getAddressLine(0).split(",")[0];
            }

            return "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
