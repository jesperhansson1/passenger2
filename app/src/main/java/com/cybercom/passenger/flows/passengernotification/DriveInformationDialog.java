package com.cybercom.passenger.flows.passengernotification;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.interfaces.FragmentSizeListener;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.repository.PassengerRepository;

public class DriveInformationDialog extends Fragment implements View.OnClickListener,
        GestureDetector.OnGestureListener{

    private static final float DEFAULT_SHOW_AND_HIDE_POSITION = 0;
    public static final int DOWN_ARROW_ROTATION = 180;
    public static final int DIALOG_ANIMATION_DURATION = 150;
    public static final int ARROW_ANIMATION_DURATION = 300;
    public static final int UP_ARROW_ANIMATION = 0;
    public static final int MARGIN = 40;

    private static final String DRIVE_KEY = "DRIVE";
    private static final String PICKUP_CONFIRMED_KEY = "PICK_UP_CONFIRMED";
    private static final float SWIPE_MIN_DISTANCE = 20;
    private static final float SWIPE_THRESHOLD_VELOCITY = 60;

    private Drive mDrive;
    private MainViewModel mMainViewModel;
    private Observer<Integer> mETAObserver;
    private LiveData<Integer> mGetETA;
    private ProgressBar mETAProgressBar;
    private TextView mETAText;
    private CardView mDriveInformationDialog;
    private boolean mIsDriveInformationDialogUp = true;
    private FrameLayout mShowAndHideArrow;
    private TextView mYouHaveBeenMatchedText;
    private boolean mPickUpConfirmed;
    private GestureDetector mGestureDetector;
    private FragmentSizeListener mFragmentSizeListener;

    public interface PassengerNotificationListener {
        void onCancelPassengerRide();
    }

    public static DriveInformationDialog getInstance(Drive drive, boolean pickedUpConfirmed) {
        DriveInformationDialog driveInformationDialog = new DriveInformationDialog();
        Bundle args = new Bundle();
        args.putSerializable(DRIVE_KEY, drive);
        args.putSerializable(PICKUP_CONFIRMED_KEY, pickedUpConfirmed);
        driveInformationDialog.setArguments(args);
        return driveInformationDialog;
    }

    private PassengerNotificationListener mPassengerNotificationListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mMainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_drive_information, container,
                false);

        mETAProgressBar = rootView.findViewById(R.id.passenger_notification_eta_progressbar);
        mETAText = rootView.findViewById(R.id.passenger_notification_eta_text);
        mYouHaveBeenMatchedText = rootView.findViewById(R.id.passenger_notification_you_have_been_matched);
        mDriveInformationDialog = rootView.findViewById(R.id.drive_information_dialog);
        Button cancelOrEarlyDropOffButton = rootView.findViewById(R.id.passenger_notification_cancel_button);

        mShowAndHideArrow = rootView.findViewById(R.id.container_driveinfodialog_arrow);

        cancelOrEarlyDropOffButton.setOnClickListener(this);

        if (getActivity() != null) {

            if (getArguments() != null) {
                mDrive = (Drive) getArguments().getSerializable(DRIVE_KEY);
                mPickUpConfirmed = getArguments().getBoolean(PICKUP_CONFIRMED_KEY);

                TextView passengerNotificationName
                        = rootView.findViewById(R.id.passenger_notification_name);
                passengerNotificationName.setText(mDrive.getDriver().getFullName());
            }

            mETAObserver = integer -> {
                if (integer != null) {
                    if(isAdded()){
                        if (integer != PassengerRepository.UNDEFINED_ETA) {
                            showETAProgressBar(false);
                            mETAText.setText(
                                    getString(R.string.eta_minutes, integer));
                        } else {
                            mETAText.setText("");
                            showETAProgressBar(true);
                        }
                    }
                }
            };

            if (mPickUpConfirmed) {
                cancelOrEarlyDropOffButton.setText(R.string.driveinformation_earlydropoff_button);
            } else {
                cancelOrEarlyDropOffButton.setText(R.string.driveinformation_cancelpassengerride_button);
            }

            mGetETA = mMainViewModel.getETA();
            mGetETA.observe(getActivity(), mETAObserver);
        }

        mGestureDetector = new GestureDetector(getContext(), this);

        mDriveInformationDialog.setOnTouchListener((v, event) ->
                mGestureDetector.onTouchEvent(event));

        mDriveInformationDialog.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mFragmentSizeListener != null) {
                        mFragmentSizeListener.onHeightChanged(
                                mDriveInformationDialog.getHeight() +
                                MARGIN);
                    }
                    mDriveInformationDialog.getViewTreeObserver().removeOnGlobalLayoutListener(
                            this);
                }
            });


        return rootView;
    }

    private void showETAProgressBar(boolean show) {
        if (show && mETAProgressBar.getVisibility() == View.INVISIBLE) {
            mETAProgressBar.setVisibility(View.VISIBLE);
        } else if (!show && mETAProgressBar.getVisibility() == View.VISIBLE) {
            mETAProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void toggleShowHideDialog() {
        if (mIsDriveInformationDialogUp) {
            mIsDriveInformationDialogUp = false;
            hideDriveInformationDialog();
        } else {
            mIsDriveInformationDialogUp = true;
            showDriveInformationDialog();
        }
    }

    public void showDriveInformationDialog() {
        mYouHaveBeenMatchedText.setVisibility(View.VISIBLE);

        if (mFragmentSizeListener != null) {
            mFragmentSizeListener
                    .onHeightChanged(mDriveInformationDialog.getHeight() + MARGIN);
        }

        mDriveInformationDialog.animate().translationY(DEFAULT_SHOW_AND_HIDE_POSITION)
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHideArrow.animate().rotation(DOWN_ARROW_ROTATION)
                .setDuration(ARROW_ANIMATION_DURATION);
    }

    public void hideDriveInformationDialog() {
        mYouHaveBeenMatchedText.setVisibility(View.INVISIBLE);

        if (mFragmentSizeListener != null) {
            mFragmentSizeListener.onHeightChanged(mShowAndHideArrow.getHeight());
        }

        mDriveInformationDialog.animate()
                .translationY(mDriveInformationDialog.getHeight() + MARGIN - mShowAndHideArrow.getHeight())
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHideArrow.animate().rotation(UP_ARROW_ANIMATION).setDuration(ARROW_ANIMATION_DURATION);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PassengerNotificationListener) {
            mPassengerNotificationListener = (PassengerNotificationListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_passenger_notification_listener,
                    Toast.LENGTH_SHORT).show();
        }

        if (context instanceof FragmentSizeListener) {
            mFragmentSizeListener = (FragmentSizeListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mGetETA.removeObserver(mETAObserver);
        mFragmentSizeListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.passenger_notification_cancel_button: {
                if (mPickUpConfirmed) {
                    // TODO: early drop-off
                } else {
                    mPassengerNotificationListener.onCancelPassengerRide();
                }
                break;
            }
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        toggleShowHideDialog();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            // bottom to top
            if (!mIsDriveInformationDialogUp) toggleShowHideDialog();
        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            // top to bottom
            if (mIsDriveInformationDialogUp) toggleShowHideDialog();
        }
        return false;
    }
}
