package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.Refund;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeRefundAsyncTask extends AsyncTask<String, Void, String> {

    private String mChargeId = null;

    private onRefundCreated mOnRefundDelegate;

    public interface onRefundCreated{
        void onRefundInitiated(String refundId);
    }

    public StripeRefundAsyncTask(String chargeId, onRefundCreated delegate) {
        mChargeId = chargeId;
        mOnRefundDelegate = delegate;
        Timber.d(" from " + mChargeId + " refund");
    }

    @Override
    protected String doInBackground(String... params) {
        String refundId = postData(mChargeId);
        Timber.d("refund id%s", refundId);
        return refundId;
    }

    @Override
    protected void onPostExecute(String refundId) {
        Timber.d("refund created %s", refundId);
        if(mOnRefundDelegate != null)
        {
            mOnRefundDelegate.onRefundInitiated(refundId);
        }
        else
        {
            Timber.d("Failed to create refund.");
        }
    }

    private String postData(String chargeId) {
        String refundId = null;
        Stripe.apiKey = STRIPE_API_KEY;

        try
        {
            Map<String, Object> params = new HashMap<>();
            params.put("charge", chargeId);
            Refund refund = Refund.create(params);
            Timber.d("charge refund %s",refund);
            refundId = refund.getId();

        }
        catch(Exception e)
        {
            Timber.d("error creating refund %s", e.getMessage());
        }
        return refundId;
    }

}