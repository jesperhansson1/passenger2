package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Refund;
import java.util.Map;
import timber.log.Timber;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

//creates and returns refund id for a given charge object
public class StripeRefundAsyncTask extends AsyncTask<String, Void, String> {

    private Map<String, Object> mMapParams;
    private onRefundCreated mOnRefundDelegate;

    public interface onRefundCreated{
        void onRefundInitiated(String refundId);
    }

    public StripeRefundAsyncTask(Map<String, Object> mapParams, onRefundCreated delegate) {
        mOnRefundDelegate = delegate;
        mMapParams = mapParams;
    }

    @Override
    protected String doInBackground(String... params) {
        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Refund refund = Refund.create(mMapParams);
            Timber.d("stripe charge refund %s",refund);
            return refund.getId();
        } catch (APIConnectionException | InvalidRequestException | AuthenticationException |
                CardException | APIException e) {
            Timber.d("stripe error creating refund %s", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String refundId) {
        Timber.d("stripe refund created %s", refundId);
        if(mOnRefundDelegate != null) {
            mOnRefundDelegate.onRefundInitiated(refundId);
        }
        else {
            Timber.d("stripe Failed to create refund.");
        }
    }
}