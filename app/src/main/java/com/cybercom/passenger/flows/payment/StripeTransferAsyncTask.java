package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Transfer;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY_SEK;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeTransferAsyncTask  extends AsyncTask<String, Void, String> {

    private int mAmount = 0;
    private String mChargeId = null;
    private String mAccountId = null;

    private onTransferCreated mOnTransferDelegate;

    public interface onTransferCreated{
        void onChargeAmountReserved(String chargeId);
    }

    public StripeTransferAsyncTask(String chargeId, int amount, StripeTransferAsyncTask.onTransferCreated delegate, String accountId) {
        mAmount = amount;
        mChargeId = chargeId;
        mAccountId = accountId;
        mOnTransferDelegate = delegate;
        Timber.d(amount  + " from " + chargeId + " to " + accountId);
    }

    @Override
    protected String doInBackground(String... params) {
        String transferId = postData(mChargeId, mAmount, mAccountId);
        Timber.d("transfer id%s", transferId);
        return transferId;
    }

    @Override
    protected void onPostExecute(String transferId) {
        Timber.d("transfer created %s", transferId);
        if(mOnTransferDelegate != null)
        {
            mOnTransferDelegate.onChargeAmountReserved(transferId);
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
