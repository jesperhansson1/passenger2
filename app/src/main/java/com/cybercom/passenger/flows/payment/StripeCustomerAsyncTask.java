package com.cybercom.passenger.flows.payment;


import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Customer;

import java.util.Map;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

//Creates stripe customer and returns id with given parameters and attaches a card for the same
public class StripeCustomerAsyncTask extends AsyncTask<String, Void, String> {

    private OnCustomerCreated mCustomerDelegate;
    private Map<String, Object> mMapParams;

    public interface OnCustomerCreated{
        void updateCustomerId(String customerId);
    }

    public StripeCustomerAsyncTask(Map<String, Object> mapParams, OnCustomerCreated delegate) {
        mMapParams = mapParams;
        mCustomerDelegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Customer customer = Customer.create(mMapParams);
            Timber.d("stripe Customer created %s", customer.toString());
            return customer.getId();
        } catch (APIConnectionException | InvalidRequestException | AuthenticationException |
                APIException | CardException e) {
            Timber.d("stripe error creating Customer %s", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String customerId) {
        Timber.d("stripe customer created %s", customerId);
        if(mCustomerDelegate!=null) {
            mCustomerDelegate.updateCustomerId(customerId);
        }
        else {
            Timber.d("stripe Failed to create customer.");
        }
    }
}