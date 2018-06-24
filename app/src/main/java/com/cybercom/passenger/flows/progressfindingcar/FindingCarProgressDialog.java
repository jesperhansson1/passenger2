package com.cybercom.passenger.flows.progressfindingcar;

import android.annotation.SuppressLint;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.payment.PaymentHelper;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.PRECISION;

public class FindingCarProgressDialog extends DialogFragment implements View.OnClickListener {

    public static final String MATCHING_IN_PROGRESS = "MATCHING_IN_PROGRESS";
    double mAmount;
    String mChargeId = null;

    public interface FindingCarListener {
        void onCancelFindingCarPressed(Boolean isCancelPressed, String chargeId);
    }

    public static FindingCarProgressDialog getInstance() {
        return new FindingCarProgressDialog();
    }

    private FindingCarListener mFindingCarListener;

    public void setAmount(double amount)
    {
        mAmount = PaymentHelper.roundToPlace(amount,PRECISION);
    }

    public void setChargeId(String chargeId)
    {
        mChargeId = chargeId;
    }

    @Override public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_progress_finding_car, container,
                false);


        TextView priceTextView = rootView.findViewById(R.id.finding_car_price_title);
        priceTextView.setText(getString(R.string.finding_car_price,
                mAmount));
        this.getDialog().setCanceledOnTouchOutside(false);
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
                mFindingCarListener.onCancelFindingCarPressed(true, mChargeId);
                dismiss();
                break;
            }
        }
    }
}
