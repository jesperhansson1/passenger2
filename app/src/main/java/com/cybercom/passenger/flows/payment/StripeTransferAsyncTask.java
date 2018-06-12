package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Transfer;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY_SEK;
import static com.cybercom.passenger.flows.payment.PaymentConstants.NOSHOW_FEE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeTransferAsyncTask  extends AsyncTask<String, Void, String> {

    private String mChargeId = null;
    private String mAccountId = null;

    private onTransferCreated mOnTransferDelegate;

    public interface onTransferCreated{
        void onTransferInitiated(String transferId);
    }

    public StripeTransferAsyncTask(String chargeId, StripeTransferAsyncTask.onTransferCreated delegate, String accountId) {
        mChargeId = chargeId;
        mAccountId = accountId;
        mOnTransferDelegate = delegate;
        Timber.d(NOSHOW_FEE  + " from " + mChargeId + " to " + mAccountId);
    }

    @Override
    protected String doInBackground(String... params) {
        String transferId = postData(mChargeId, NOSHOW_FEE, mAccountId);
        Timber.d("transfer id%s", transferId);
        return transferId;
    }

    @Override
    protected void onPostExecute(String transferId) {
        Timber.d("transfer created %s", transferId);
        if(mOnTransferDelegate != null)
        {
            mOnTransferDelegate.onTransferInitiated(transferId);
        }
        else
        {
            Timber.d("Failed to create transfer.");
        }
    }

    private String postData(String chargeId, int amount, String accountId) {
        String transferId = null;
        Stripe.apiKey = STRIPE_API_KEY;

        try
        {
            Charge charge = Charge.retrieve(chargeId);
            charge.capture();

            Map<String, Object> transferParams = new HashMap<>();
            transferParams.put("amount", amount);
            transferParams.put("currency", CURRENCY_SEK);
            transferParams.put("source_transaction", chargeId);
            transferParams.put("destination", accountId);
            Transfer transfer = Transfer.create(transferParams);
            Timber.d("transfer %s", transfer);
            transferId = transfer.getId();
        }
        catch(Exception e)
        {
            Timber.d("error creating transfer %s", e.getMessage());
        }
        return transferId;
    }

}
