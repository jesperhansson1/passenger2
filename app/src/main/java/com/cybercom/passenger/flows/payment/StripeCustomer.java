package com.cybercom.passenger.flows.payment;


import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.model.Customer;

import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.Constants.STRIPE_API_KEY;

public class StripeCustomer extends AsyncTask<String, Void, String> {
    String mToken = null;
    String mCustomerId = null;
    String mEmail = null;

    OnCustomerCreated mCustomerDelegate;

    public interface OnCustomerCreated{
        void updateCustomerId(String customerId);
    }

    public StripeCustomer(String tokenId, String email, OnCustomerCreated delegate) {
        mToken = tokenId;
        mEmail = email;
        mCustomerDelegate = delegate;
        Timber.d(tokenId.toString());
    }

    public StripeCustomer() {

    }

    @Override
    protected String doInBackground(String... params) {
        mCustomerId =  postData(mToken, mEmail);
        Timber.d("customer id" + mCustomerId.toString());
        return mCustomerId;
    }

    @Override
    protected void onPostExecute(String customerId) {
        Timber.d("customer created " + customerId);
        mCustomerDelegate.updateCustomerId(customerId);


    }

    public String postData(String tokenId, String email) {
        Customer customer = null;
        Stripe.apiKey = STRIPE_API_KEY;
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("email", email);
        customerParams.put("source", tokenId);
        try
        {
            customer = Customer.create(customerParams);
            Timber.d("Customer created " + customer.toString());
            return customer.getId();

        }
        catch(Exception e)
        {
            Timber.d("error creating Customer " + e.getMessage());
        }
        return customer.getId();
    }
}