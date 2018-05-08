package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY1;

public class StripeCharge extends AsyncTask<String, Void, String> {
    String mToken;
    String mDescription = "test";
    String mDriverAmount = "0";
    String mTotalAmount = "0";
    String mConnectedAccount = "acct_1CPENbJPkd6VZmZY";

    public StripeCharge(String token, String description, double driverAmount, double totalAmount) {
        mToken = token;
        mDescription = description;
        mDriverAmount =  String.valueOf(Math.round(driverAmount));
        mTotalAmount =  String.valueOf(Math.round(totalAmount));
        System.out.println(mDriverAmount + " " + mTotalAmount);
    }
    public StripeCharge()
    {

    }

    @Override
    protected String doInBackground(String... params) {
        new Thread() {
            @Override
            public void run() {
                postData(mDescription,mToken,mDriverAmount,mTotalAmount);

            }
        }.start();
        return "Done";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Timber.d(s);
    }

    public void postData(String description, String token, String driverAmount, String totalAmount ) {

        Stripe.apiKey = STRIPE_API_KEY;

        try {

            //Sending charge for platform alone
/*
            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", amount);
            chargeParams.put("currency", "sek");
            chargeParams.put("description", description);
            chargeParams.put("source", token);
            Charge.create(chargeParams);
*/
//Sending charge for platform and connected accounts


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("amount", totalAmount);
            params.put("currency", "sek");
            params.put("source", token);
            Map<String, Object> destinationParams = new HashMap<String, Object>();
            destinationParams.put("amount", driverAmount);
            destinationParams.put("account", mConnectedAccount);
            params.put("destination", destinationParams);
            Charge charge = Charge.create(params);

        }
        catch(Exception e)
        {
            Timber.e(e.getLocalizedMessage());
        }
    }

}
