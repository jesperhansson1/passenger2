package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Transfer;
import java.util.Map;
import timber.log.Timber;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER_SOURCE_TRANSACTION;

public class StripeTransferAsyncTask  extends AsyncTask<String, Void, String> {

    private Map<String, Object> mMapParams;
    private onTransferCreated mOnTransferDelegate;

    public interface onTransferCreated{
        void onTransferInitiated(String transferId);
    }

    public StripeTransferAsyncTask(Map<String, Object> mapParams, onTransferCreated delegate) {
        mOnTransferDelegate = delegate;
        mMapParams = mapParams;
    }

    @Override
    protected String doInBackground(String... params) {
        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Charge charge = Charge.retrieve((String) mMapParams.get(TRANSFER_SOURCE_TRANSACTION));
            charge.capture();
            Transfer transfer = Transfer.create(mMapParams);
            Timber.d("stripe transfer %s", transfer);
            return transfer.getId();
        } catch (APIConnectionException | InvalidRequestException | AuthenticationException |
                APIException | CardException e) {
            Timber.d("stripe error creating transfer %s", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String transferId) {
        Timber.d("stripe transfer created %s", transferId);
        if(mOnTransferDelegate != null) {
            mOnTransferDelegate.onTransferInitiated(transferId);
        }
        else {
            Timber.d("stripe Failed to create transfer.");
        }
    }
}
