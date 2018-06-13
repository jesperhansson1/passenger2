package com.cybercom.passenger.flows.payment;


import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.model.Customer;

import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeCustomerAsyncTask extends AsyncTask<String, Void, String> {
    private String mToken = null;
    private String mEmail = null;

    private OnCustomerCreated mCustomerDelegate;

    public interface OnCustomerCreated{
        void updateCustomerId(String customerId);
    }

    public StripeCustomerAsyncTask(String tokenId, String email, OnCustomerCreated delegate) {
        mToken = tokenId;
        mEmail = email;
        mCustomerDelegate = delegate;
        Timber.d(tokenId);
    }

    @Override
    protected String doInBackground(String... params) {
        String customerId = postData(mToken, mEmail);
        Timber.d("stripe customer id%s", customerId);
        return customerId;
    }

    @Override
    protected void onPostExecute(String customerId) {
        Timber.d("stripe customer created %s", customerId);
        mCustomerDelegate.updateCustomerId(customerId);


    }

    private String postData(String tokenId, String email) {
        Customer customer;
        Stripe.apiKey = STRIPE_API_KEY;
        String customerId = null;
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("email", email);
        customerParams.put("source", tokenId);
        try
        {
            customer = Customer.create(customerParams);
            Timber.d("stripe Customer created %s", customer.toString());
            customerId =  customer.getId();
        }
        catch(Exception e)
        {
            Timber.d("stripe error creating Customer %s", e.getMessage());
        }
        return customerId;
    }
}