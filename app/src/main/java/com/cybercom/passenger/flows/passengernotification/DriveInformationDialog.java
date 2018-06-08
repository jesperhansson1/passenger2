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
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainViewModel;
import com.cybercom.passenger.model.Drive;

import timber.log.Timber;

public class DriveInformationDialog extends DialogFragment implements View.OnClickListener {

    private static final float DEFAULT_SHOW_AND_HIDE_POSITION = 0;
    public static final int DOWN_ARROW_ROTATION = 180;
    public static final int DIALOG_ANIMATION_DURATION = 150;
    public static final int ARROW_ANIMATION_DURATION = 300;
    public static final int UP_ARROW_ANIMATION = 0;
    public static final int MARGIN = 40;

    private static final String DRIVE_KEY = "DRIVE";
    public static final String TAG = "PASSENGER_NOTIFICATION_DIALOG";
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

    public interface PassengerNotificationListener {
        void onCancelDrive(Drive drive);
    }

    public static DriveInformationDialog getInstance(Drive drive) {
        DriveInformationDialog driveInformationDialog = new DriveInformationDialog();
        Bundle args = new Bundle();
        args.putSerializable(DRIVE_KEY, drive);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_drive_information, container,
                false);

        setCancelable(false);

        mETAProgressBar = rootView.findViewById(R.id.passenger_notification_eta_progressbar);
        mETAText = rootView.findViewById(R.id.passenger_notification_eta_text);
        mYouHaveBeenMatchedText = rootView.findViewById(R.id.passenger_notification_you_have_been_matched);
        mDriveInformationDialog = rootView.findViewById(R.id.drive_information_dialog);
//        mDriveInformationDialog.setOnClickListener(v -> {
//            if (!mIsDriveInformationDialogUp) {
//                toggleShowHideDialog();
//            }
//        });
        mShowAndHideArrow = rootView.findViewById(R.id.container_driveinfodialog_arrow);
        mShowAndHideArrow.setOnClickListener(v -> toggleShowHideDialog());

        Button cancelButton = rootView.findViewById(R.id.passenger_notification_cancel_button);
        cancelButton.setOnClickListener(this);

        if (getActivity() != null) {

            if (getArguments() != null) {
                mDrive = (Drive) getArguments().getSerializable(DRIVE_KEY);

                TextView passengerNotificationName
                        = rootView.findViewById(R.id.passenger_notification_name);
                passengerNotificationName.setText(mDrive.getDriver().getFullName());
            }

            mETAObserver = integer -> {
                Timber.i("eta obser: %d", integer);
                if (integer != null) {
                    if(isAdded()){
                        mETAProgressBar.setVisibility(View.GONE);
                        mETAText.setText(
                                getString(R.string.eta_minutes, integer));
                    }
                }
            };

            mGetETA = mMainViewModel.getETA();
            mGetETA.observe(getActivity(), mETAObserver);
        }

        return rootView;
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

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getView().post(() -> {

            Window dialogWindow = getDialog().getWindow();

            dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            getView().invalidate();
        });
    }

    public void showDriveInformationDialog() {
        mYouHaveBeenMatchedText.setVisibility(View.VISIBLE);

        mDriveInformationDialog.animate().translationY(DEFAULT_SHOW_AND_HIDE_POSITION)
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHideArrow.animate().rotation(DOWN_ARROW_ROTATION)
                .setDuration(ARROW_ANIMATION_DURATION);
    }

    public void hideDriveInformationDialog() {
        mYouHaveBeenMatchedText.setVisibility(View.INVISIBLE);

        mDriveInformationDialog.animate()
                .translationY(mDriveInformationDialog.getHeight() + MARGIN - mShowAndHideArrow.getHeight())
                .setDuration(DIALOG_ANIMATION_DURATION);
        mShowAndHideArrow.animate().rotation(UP_ARROW_ANIMATION).setDuration(ARROW_ANIMATION_DURATION);
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
                mPassengerNotificationListener.onCancelDrive(mDrive);
                dismiss();
                break;
            }
        }
    }
}
