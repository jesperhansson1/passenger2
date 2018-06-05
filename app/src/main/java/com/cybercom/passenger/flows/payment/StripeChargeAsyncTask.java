package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeChargeAsyncTask extends AsyncTask<String, Void, String> {

    double mAmount = 0.0;
    String mCustomerId = null;
    boolean mStatus = false;
    String mChargeId = null;

    onChargeCreated mChargeDelegate;

    public interface onChargeCreated{
        void chargeBlockedStatus(String chargeId);
    }

    public StripeChargeAsyncTask(String customerId, double amount, onChargeCreated delegate, boolean capture) {
        mAmount = amount;
        mCustomerId = customerId;
        mChargeDelegate = delegate;
        mStatus = capture;
        Timber.d(amount  + " to " + customerId);
    }

    public StripeChargeAsyncTask() {

    }

    @Override
    protected String doInBackground(String... params) {
        mChargeId =  postData(mCustomerId, mAmount, mStatus);
        Timber.d("charge id" + mChargeId);
        return mChargeId;
    }

    @Override
    protected void onPostExecute(String chargeId) {
        Timber.d("amount blocked " + chargeId);
        mChargeDelegate.chargeBlockedStatus(chargeId);

    }

    public String postData(String customerId, double amount, boolean status) {
        String chargeId = null;
        Stripe.apiKey = STRIPE_API_KEY;

        amount = amount * 100;
        Timber.d("val "  + (int)amount);

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount",  (int)amount); // $15.00 this time
        chargeParams.put("currency", "sek");
        chargeParams.put("capture", status);
        chargeParams.put("customer", customerId); // Previously stored, then retrieved


        try
        {
            Charge charge = Charge.create(chargeParams);
            Timber.d("Charge created " + charge);
            return charge.getId();
        }
        catch(Exception e)
        {
            Timber.d("error creating charge " + e.getMessage());
        }
        return chargeId;
    }
}
