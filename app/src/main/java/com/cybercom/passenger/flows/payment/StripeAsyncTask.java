package com.cybercom.passenger.flows.payment;


import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.FileUpload;
import com.stripe.model.Refund;
import com.stripe.model.Token;
import com.stripe.model.Transfer;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.ACCOUNT;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_EXTERNAL_ACCOUNT;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CUSTOMER;
import static com.cybercom.passenger.flows.payment.PaymentConstants.FILE_UPLOAD;
import static com.cybercom.passenger.flows.payment.PaymentConstants.REFUND;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RESERVE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.SPLIT_CHAR;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TOKEN;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER_SOURCE_TRANSACTION;

public class StripeAsyncTask extends AsyncTask<String, Void, String> {
    private StripeAsyncTaskDelegate mStripeAsyncTaskDelegate;
    private Map<String, Object> mMapParams;
    private String mChoice;

    public interface StripeAsyncTaskDelegate{
        void onStripeTaskCompleted(String result);
    }

    public StripeAsyncTask(Map<String, Object> mapParams, StripeAsyncTaskDelegate caller, String choice) {
        mStripeAsyncTaskDelegate = caller;
        mMapParams = mapParams;
        mChoice = choice;
    }

    @Override
    protected String doInBackground(String... params) {
        Stripe.apiKey = STRIPE_API_KEY;
        String result = null;
        try {
            switch (mChoice)
            {
                case TOKEN:
                    Token token = Token.create(mMapParams);
                    Timber.d("stripe token created %s", token);
                    result =  token.getId();
                    break;
                case TRANSFER:
                    Charge charge = Charge.retrieve((String) mMapParams.get(TRANSFER_SOURCE_TRANSACTION));
                    charge.capture();
                    Transfer transfer = Transfer.create(mMapParams);
                    Timber.d("stripe transfer %s", transfer);
                    result =  transfer.getId();
                    break;
                case REFUND:
                    Refund refund = Refund.create(mMapParams);
                    Timber.d("stripe charge refund %s",refund);
                    result =  refund.getId();
                    break;
                case FILE_UPLOAD:
                    Stripe.apiKey = STRIPE_API_KEY;
                    FileUpload fileUpload = FileUpload.create(mMapParams);
                    Timber.d("stripe fileupload created %s", fileUpload);
                    result =  fileUpload.getId();
                    break;
                case CUSTOMER:
                    Customer customer = Customer.create(mMapParams);
                    Timber.d("stripe Customer created %s", customer);
                    result =  customer.getId();
                    break;
                case RESERVE:
                    Charge reserve = Charge.create(mMapParams);
                    Timber.d("stripe charge created %s", reserve);
                    result =  reserve.getId();
                    break;
                case ACCOUNT:
                    Account account = Account.create(mMapParams);
                    result = account.getId();
                    Timber.d("stripe account created %s", account);
                    Map<String, Object> external_account_params = new HashMap<>();
                    external_account_params.put(CONNECT_EXTERNAL_ACCOUNT, params[0]);//mTokenId);
                    account.getExternalAccounts().create(external_account_params);
                    Timber.d("stripe account created with card %s", account);
                    break;
                default:
                    break;
            }
        } catch (APIConnectionException | InvalidRequestException | AuthenticationException |
                CardException | APIException e) {
            Timber.d("stripe error creating token %s",  e.getLocalizedMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Timber.d("stripe task created %s", result);
        if(mStripeAsyncTaskDelegate != null) {
            mStripeAsyncTaskDelegate.onStripeTaskCompleted(result + SPLIT_CHAR + mChoice);
        }
        else {
            Timber.d("stripe Failed to create task.");
        }
    }
}
