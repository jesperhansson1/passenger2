package com.cybercom.passenger.flows.progressfindingcar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cybercom.passenger.R;

import timber.log.Timber;

public class FindingCarProgressDialog extends DialogFragment implements View.OnClickListener {

    public interface FindingCarListener {
        void onCancelPressed(Boolean isCancelPressed);
    }

    public static FindingCarProgressDialog getInstance() {
        return new FindingCarProgressDialog();
    }

    private FindingCarListener mFindingCarListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.finding_car_progress_dialog, container,
                false);

        Button cancelButton = rootView.findViewById(R.id.finding_car_cancel);
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

            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FindingCarListener) {
            mFindingCarListener = (FindingCarListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_finding_car_listener,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.finding_car_cancel:{
                Timber.i("Finding car cancel button pressed");
                mFindingCarListener.onCancelPressed(true);
                dismiss();
                break;
            }
        }
    }
}
