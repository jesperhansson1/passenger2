package com.cybercom.passenger.flows.dropofffragment;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.PassengerRide;
import com.cybercom.passenger.repository.PassengerRepository;
import com.cybercom.passenger.utils.RoundCornersTransformation;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

import static com.cybercom.passenger.utils.RoundCornersTransformation.RADIUS;

public class DriverDropOffFragment extends Fragment implements View.OnClickListener {

    public static final String DRIVER_DROP_OFF_FRAGMENT_TAG = "DRIVER_DROP_OFF_FRAGMENT";
    public static final String PASSENGER_RIDE_KEY = "PASSENGER";
    private DriverDropOffConfirmationListener mDriverDropOffConfirmationListener;
    private PassengerRide mPassengerRide;

    public interface DriverDropOffConfirmationListener{
        void onDropOffConfirmation(PassengerRide passengerRide);
        void onDropOffCanceled(PassengerRide passengerRide);
    }

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

    @SuppressLint("TimberArgCount")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_drop_off, container, false);

        if (getArguments() != null) {
            mPassengerRide = (PassengerRide) getArguments()
                    .getSerializable(PASSENGER_RIDE_KEY);

            if (mPassengerRide != null) {
                TextView driverDropOffTitle = view.findViewById(R.id.fragment_driver_drop_off_title);
                String passengerFirstName = mPassengerRide.getPassenger().getFullName().split(" ")[0];
                driverDropOffTitle.setText(getString(R.string.fragment_driver_drop_off_title,
                        passengerFirstName));
                TextView passengerName
                        = view.findViewById(R.id.fragment_driver_drop_off_passenger_name);
                passengerName.setText(mPassengerRide.getPassenger().getFullName());
                ImageView passengerImageView = view.findViewById(R.id.fragment_driver_drop_off_passenger_image);
                LiveData<Uri> imageUri = PassengerRepository.getInstance().getImageUri(mPassengerRide.getPassenger().getUserId());
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
            }
        }

        Button confirmDropOff = view.findViewById(R.id.fragment_driver_drop_off_button_confirm);
        confirmDropOff.setOnClickListener(this);
        Button cancelDropOff = view.findViewById(R.id.fragment_driver_drop_off_button_cancel);
        cancelDropOff.setOnClickListener(this);



        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof DriverDropOffConfirmationListener){
            mDriverDropOffConfirmationListener = (DriverDropOffConfirmationListener) context;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.fragment_driver_drop_off_button_confirm:{
                mDriverDropOffConfirmationListener.onDropOffConfirmation(mPassengerRide);
                break;
            }
            case R.id.fragment_driver_drop_off_button_cancel:{
                mDriverDropOffConfirmationListener.onDropOffCanceled(mPassengerRide);
                break;
            }
        }
    }
}
