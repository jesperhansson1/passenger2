package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import java.util.Map;
import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeChargeReserveAsyncTask extends AsyncTask<String, Void, String> {

    private Map<String, Object> mMapParams;
    private onChargeCreated mChargeDelegate;

    public interface onChargeCreated{
        void onChargeAmountReserved(String chargeId);
    }

    public StripeChargeReserveAsyncTask(Map<String, Object> mapParams, onChargeCreated delegate) {
        mChargeDelegate = delegate;
        mMapParams = mapParams;
    }

    @Override
    protected String doInBackground(String... params) {
        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Charge charge = Charge.create(mMapParams);
            Timber.d("stripe charge created %s", charge);
            return charge.getId();
        } catch (APIConnectionException | InvalidRequestException | APIException |
                AuthenticationException | CardException e) {
            Timber.d("stripe error creating charge %s", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String chargeId) {
        if(mChargeDelegate != null) {
            mChargeDelegate.onChargeAmountReserved(chargeId);
        }
        else {
            Timber.d("stripe charge delegate is null.");
        }
    }
}
