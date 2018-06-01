package com.cybercom.passenger.flows.payment;


import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.Token;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.Constants.STRIPE_API_KEY;

public class StripeCustomer extends AsyncTask<String, Void, Customer> {
    Token mToken = null;
    Customer mCustomer = null;

    public StripeCustomer(Token token) {
        mToken = token;
        Timber.d(token.toString());
    }

    public StripeCustomer() {

    }

    @Override
    protected Customer doInBackground(String... params) {
        mCustomer =  postData(mToken);
        Timber.d("customer id" + mCustomer.toString());
        return mCustomer;
    }

    @Override
    protected void onPostExecute(Customer customer) {
        super.onPostExecute(customer);
        Timber.d("customer created " + customer.toString());
    }

    public Customer postData(Token token) {
        Stripe.apiKey = STRIPE_API_KEY;
        String tokens =token.getId();
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", "iouerowior");
        customerParams.put("source", tokens);
        try
        {
            mCustomer = Customer.create(customerParams);
            Timber.d("Customer created " + mCustomer.toString());
            return mCustomer;

        }
        catch(Exception e)
        {
            Timber.d("error creating Customer " + e.getMessage());
        }
        return mCustomer;
    }
}