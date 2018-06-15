package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Token;
import java.util.Map;
import timber.log.Timber;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

//creates and returns token id for a given card object
public class StripeTokenAsyncTask extends AsyncTask<String, Void, String> {
    private OnTokenCreated mTokenDelegate;
    private Map<String, Object> mMapParams;

    public interface OnTokenCreated{
        void updateTokenId(String tokenId);
    }

    public StripeTokenAsyncTask(Map<String, Object> mapParams, OnTokenCreated caller) {
        mTokenDelegate = caller;
        mMapParams = mapParams;
    }

    @Override
    protected String doInBackground(String... params) {
        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Token token = Token.create(mMapParams);
            Timber.d("stripe token created %s", token.toString());
            return token.getId();
        } catch (APIConnectionException | InvalidRequestException | AuthenticationException |
                CardException | APIException e) {
            Timber.d("stripe error creating token %s",  e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String token) {
        Timber.d("stripe token created %s", token);
        if(mTokenDelegate != null) {
            mTokenDelegate.updateTokenId(token);
        }
        else {
            Timber.d("stripe Failed to create token.");
        }
    }

}


