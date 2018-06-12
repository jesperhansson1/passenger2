package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Transfer;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY_SEK;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeChargeAsyncTask extends AsyncTask<String, Void, String> {

    private int mAmount = 0;
    private String mCustomerId = null;
    private boolean mStatus = false;

    private onChargeCreated mChargeDelegate;

    public interface onChargeCreated{
        void onChargeAmountReserved(String chargeId);
    }

    public StripeChargeAsyncTask(String customerId, int amount, onChargeCreated delegate, boolean capture) {
        mAmount = amount;
        mCustomerId = customerId;
        mChargeDelegate = delegate;
        mStatus = capture;
        Timber.d(amount  + " to " + customerId);
    }

    @Override
    protected String doInBackground(String... params) {
        String chargeId = postData(mCustomerId, mAmount, mStatus);
        Timber.d("charge id%s", chargeId);
        return chargeId;
    }

    @Override
    protected void onPostExecute(String chargeId) {
        Timber.d("amount blocked %s", chargeId);
        if(mChargeDelegate != null)
        {
            mChargeDelegate.onChargeAmountReserved(chargeId);
        }
        else
        {
            Timber.d("charge delegate is null.");
        }
    }

    private String postData(String customerId, int amount, boolean status) {
        String chargeId = null;
        Stripe.apiKey = STRIPE_API_KEY;
        Timber.d("amount is  %s", amount);
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount",  amount);
        chargeParams.put("currency", CURRENCY_SEK);
        chargeParams.put("capture", status);
        chargeParams.put("customer", customerId); 

        try
        {
            Charge charge = Charge.create(chargeParams);
            Timber.d("Charge created %s", charge);
            chargeId = charge.getId();
        }
        catch(Exception e)
        {
            Timber.d("error creating charge %s", e.getMessage());
        }
        return chargeId;
    }
}
