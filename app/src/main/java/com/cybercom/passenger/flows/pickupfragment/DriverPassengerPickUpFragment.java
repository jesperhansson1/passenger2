package com.cybercom.passenger.flows.pickupfragment;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.PassengerRide;
import com.cybercom.passenger.repository.PassengerRepository;
import com.cybercom.passenger.utils.RoundCornersTransformation;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

import static com.cybercom.passenger.model.User.TYPE_DRIVER;
import static com.cybercom.passenger.model.User.TYPE_PASSENGER;
import static com.cybercom.passenger.utils.RoundCornersTransformation.RADIUS;

public class DriverPassengerPickUpFragment extends Fragment implements View.OnClickListener {

    public static final String DRIVER_PASSENGER_PICK_UP_FRAGMENT_TAG
            = "DRIVER_PASSENGER_PICK_UP_FRAGMENT";
    private static final String PASSENGER_RIDE_KEY = "PASSENGER_RIDE";
    public static final int TIME_BEFORE_PASSENGER_NEEDS_TO_COME_TO_THE_CAR_IN_MILLISECONDS = 120000;
    public static final int COUNT_DOWN_INTERVAL = 1000;

    private FragmentSizeListener mFragmentSizeListener;
    private DriverPassengerPickUpButtonClickListener mDriverPassengerPickUpButtonClickListener;

    private Button mNoShowButton;

    private PassengerRide mPassengerRide;
    private Drive mDrive;
    private TextView mTextViewTimeToGoTitle;
    private TextView mTextViewTimeToGoCountDown;
    Bundle mArgs = new Bundle();

    public interface DriverPassengerPickUpButtonClickListener {
        void onPickUpConfirmed(PassengerRide passengerRide);

        void onPickUpNoShow(PassengerRide passengerRide);
    }

    public static DriverPassengerPickUpFragment newInstance(PassengerRide passengerRide) {
        Bundle args = new Bundle();
        args.putSerializable(PASSENGER_RIDE_KEY, passengerRide);
        DriverPassengerPickUpFragment fragment = new DriverPassengerPickUpFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @SuppressLint("TimberArgCount")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_passenger_pick_up, container, false);

        TextView name = view.findViewById(R.id.fragment_driver_passenger_pick_up_name);
        ImageView passengerImageView = view.findViewById(R.id.fragment_driver_passenger_pick_up_profile_image);

        PassengerRepository.getInstance().getUser().observe(this,myUSer-> {
            if(myUSer!=null) {
                // Driver side pick up confirmation
                if (myUSer.getType() == TYPE_DRIVER){
                    Timber.d("Pickup confirmation on driver side");
                    if (getArguments() != null) {
                        mPassengerRide = (PassengerRide) getArguments().getSerializable(PASSENGER_RIDE_KEY);
                        if (mPassengerRide != null) {
                            name.setText(mPassengerRide.getPassenger().getFullName());
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
                }
                // Passenger side pick up confirmation
                if (myUSer.getType() == TYPE_PASSENGER){
                    Timber.d("Pickup confirmation on Passenger side");
                    if (getArguments() != null) {
                        mPassengerRide = (PassengerRide) getArguments().getSerializable(PASSENGER_RIDE_KEY);
                        if (mPassengerRide != null) {
                            name.setText(mPassengerRide.getDrive().getDriver().getFullName());
                            LiveData<Uri> imageUri = PassengerRepository.getInstance().getImageUri(mPassengerRide.getDrive().getDriver().getUserId());
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
                }
            }
        });

        Button mConfirmPickUp
                = view.findViewById(R.id.fragment_driver_passenger_pick_up_confirmation_button);
        mConfirmPickUp.setOnClickListener(this);

        mNoShowButton = view.findViewById(R.id.fragment_driver_passenger_pick_up_no_show_button);
        mNoShowButton.setEnabled(false);
        mNoShowButton.setOnClickListener(this);

        mTextViewTimeToGoTitle = view.findViewById(R.id.fragment_driver_passenger_pick_up_time_to_go);
        mTextViewTimeToGoCountDown = view.findViewById(R.id.fragment_driver_passenger_pick_up_countdown);

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
                mTextViewTimeToGoTitle.setVisibility(View.GONE);
                mTextViewTimeToGoCountDown.setVisibility(View.GONE);
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
                mDriverPassengerPickUpButtonClickListener.onPickUpConfirmed(mPassengerRide);
                mFragmentSizeListener.onHeightChanged(0);
                break;
            }
            case R.id.fragment_driver_passenger_pick_up_no_show_button: {
                mDriverPassengerPickUpButtonClickListener.onPickUpNoShow(mPassengerRide);
                mFragmentSizeListener.onHeightChanged(0);
                break;
            }
        }
    }
}
