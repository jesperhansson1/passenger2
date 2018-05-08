package com.cybercom.passenger.flows.payment;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.payment.PriceDistance.ResultDistanceMatrix;
import com.cybercom.passenger.model.RideFare;
import com.google.android.gms.maps.model.LatLng;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.APP_SHARE;
import static com.cybercom.passenger.model.ConstantValues.DRIVER_SHARE;
import static com.cybercom.passenger.model.ConstantValues.PRICE;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY1;

public class PaymentActivity extends AppCompatActivity {

    CardInputWidget mCardInputWidget;
    Card mCardToSave;
    LifecycleOwner myLife;
    PaymentViewModel mPaymentViewModel;
    TextView mTextViewCharge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        myLife = this;
        mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget_payment);
        mTextViewCharge = findViewById(R.id.textView_payment_distanceamount);
        findViewById(R.id.button_payment_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });
        findViewById(R.id.button_payment_calculate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePayment();
            }
        });
        mPaymentViewModel = ViewModelProviders.of(this).get(PaymentViewModel.class);
    }

    public void calculatePayment()
    {
        LatLng start = new LatLng(55.604981,13.003822);
        LatLng end = new LatLng(56.046467,12.694512);
        mPaymentViewModel.getRideFare(start,end).observe(myLife, new Observer<RideFare>() {

            @Override
            public void onChanged(@Nullable RideFare rideFare) {
                Timber.d("Distance :" + rideFare.getDistance());
                Timber.d("Duration :" + rideFare.getDuration());
                Timber.d("Destination :" + rideFare.getEndLocation());
                Timber.d("Source :" + rideFare.getStartLocation());

                mTextViewCharge.setText(getCharge(rideFare.getDistance()));
            }
        });

    }

    public double mDriverAmount = 0.0,mAppAmount=0.0,mTotalAmount=0.0;

    public String getCharge(String dist)
    {
        //distance is in meters
        //meter to km
        System.out.println("distance is " + dist + " in meter ");
        System.out.println("distance is " + Double.valueOf(dist)/1000 + " in km ");
        mTotalAmount = (Double.valueOf(dist)/1000)*PRICE;
        mAppAmount = (Double.valueOf(dist)/1000)*APP_SHARE;
        mDriverAmount = (Double.valueOf(dist)/1000)*DRIVER_SHARE;

        System.out.println("Total amount " + (Double.valueOf(dist)/1000)*PRICE + " 35kr per 10 km ");
        System.out.println("Total amount " + (Double.valueOf(dist)/1000)*DRIVER_SHARE + " 18.5kr per 10 km driver  ");
        System.out.println("Total amount " + (Double.valueOf(dist)/1000)*APP_SHARE + " 16.5kr per 10 km pass");

        double dd = Double.valueOf(dist)*PRICE/10;
        return String.valueOf(Math.round(dd));
    }

    public void startPayment()
    {
        mCardToSave = mCardInputWidget.getCard();

        if (mCardToSave == null) {
            showToast("Invalid Card Data");
        }
        else
        {
            if (!mCardToSave.validateCard()) {
                // Do not continue token creation.
                showToast("Invalid Card Data");
            }
            else
            {
                // Do not continue token creation.
                showToast("valid Card Data");
                payC();
            }
        }
    }

    public void showToast(String errString)
    {
        Toast.makeText(getApplicationContext(),errString,Toast.LENGTH_LONG).show();
    }

    public void payC()
    {
        PaymentConfiguration.init(STRIPE_API_KEY1);
        new Stripe(getApplicationContext()).createToken(
                mCardToSave,
                PaymentConfiguration.getInstance().getPublishableKey(),
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        Timber.d("token " + token);
                       // new StripeCharge(token.getId(),"test",mTextViewCharge.getText().toString()).execute();
                        new StripeCharge(token.getId(),"test",mDriverAmount,mTotalAmount).execute();
                    }
                    public void onError(Exception error) {
                        Timber.e("error " + error.getLocalizedMessage());
                    }
                });
    }
}
