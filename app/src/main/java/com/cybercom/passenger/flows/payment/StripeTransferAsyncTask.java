package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.model.Transfer;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY_SEK;
import static com.cybercom.passenger.flows.payment.PaymentConstants.NOSHOW_FEE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.NO_SHOW_AVGIFT;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RIDE_AVGIFT;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeTransferAsyncTask  extends AsyncTask<String, Void, String> {

    private String mChargeId = null;
    private String mAccountId = null;
    private int mPaymentType = RIDE_AVGIFT;
    private int mAmount = 0;

    private onTransferCreated mOnTransferDelegate;

    public interface onTransferCreated{
        void onTransferInitiated(String transferId);
    }

    public StripeTransferAsyncTask(String chargeId, int amount, StripeTransferAsyncTask.onTransferCreated delegate, String accountId, int paymentType) {
        mChargeId = chargeId;
        mAccountId = accountId;
        mOnTransferDelegate = delegate;
        mPaymentType = paymentType;
        mAmount = amount;
        Timber.d(" from " + mChargeId + " to " + mAccountId + "charge " + mAmount + " payment type " + mPaymentType);
    }

    @Override
    protected String doInBackground(String... params) {
        String transferId = postData(mChargeId, mAmount, mAccountId,mPaymentType);
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

    private String postData(String chargeId, int amount, String accountId, int mPaymentType) {
        String transferId = null;
        Stripe.apiKey = STRIPE_API_KEY;

        if(mPaymentType == NO_SHOW_AVGIFT)
        {
            amount = NOSHOW_FEE;
        }
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

            if(mPaymentType == NO_SHOW_AVGIFT)
            {
                Map<String, Object> params = new HashMap<>();
                params.put("charge", chargeId);
                Refund refund = Refund.create(params);
                Timber.d("charge refund %s",refund);
            }

        }
        catch(Exception e)
        {
            Timber.d("error creating transfer %s", e.getMessage());
        }
        return transferId;
    }

}
