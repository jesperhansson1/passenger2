package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;

public class StripeCharge extends AsyncTask<String, Void, String> {
    String mToken;
    String mDescription = "test";
    String mAmount = "20";

    public StripeCharge(String token, String description, String amount) {
        mToken = token;
        mDescription = description;
        mAmount = amount;
    }
    public StripeCharge()
    {

    }

    @Override
    protected String doInBackground(String... params) {
        new Thread() {
            @Override
            public void run() {
                postData(mDescription,mToken,mAmount);

            }
        }.start();
        return "Done";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Timber.d(s);
    }

    public void postData(String description, String token, String amount) {

        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", amount);
            chargeParams.put("currency", "sek");
            chargeParams.put("description", description);
            chargeParams.put("source", token);
            Charge.create(chargeParams);
        }
        catch(Exception e)
        {
            Timber.e(e.getLocalizedMessage());
        }
    }

}
