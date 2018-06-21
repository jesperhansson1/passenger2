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
import static com.cybercom.passenger.flows.payment.PaymentConstants.RETRIEVE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.SPLIT_CHAR;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TOKEN;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER_REFUND;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER_SOURCE_TRANSACTION;
import static com.cybercom.passenger.flows.payment.PaymentHelper.createRefundHashMap;

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
                    chargeCapture((String) mMapParams.get(TRANSFER_SOURCE_TRANSACTION));
                    result = transferAmount(mMapParams);
                    Timber.d("stripe transfer %s", result);
                    break;
                case REFUND:
                    result =  refundAmount(mMapParams);
                    Timber.d("stripe charge refund %s",result);
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
                case RETRIEVE:
                    Charge charge1 = Charge.retrieve(params[0]);
                    result = String.valueOf(charge1.getAmount());
                    break;
                case TRANSFER_REFUND:
                    chargeCapture((String) mMapParams.get(TRANSFER_SOURCE_TRANSACTION));
                    transferAmount(mMapParams);
                    Map<String, Object> refundParams = new HashMap<>();
                    refundParams.put("charge", (String) mMapParams.get(TRANSFER_SOURCE_TRANSACTION));
                    result =  refundAmount(refundParams);
                    Timber.d("stripe charge refund %s",result);
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

    private String chargeCapture(String chargeId){
        String result = null;
        try{
            Charge charge = Charge.retrieve(chargeId);
            charge.capture();
            result = charge.getId();
        } catch (APIConnectionException e) {
            Timber.d("stripe error charge capture %s",  e.getLocalizedMessage());
        } catch (InvalidRequestException e) {
            Timber.d("stripe error charge capture %s",  e.getLocalizedMessage());
        } catch (AuthenticationException e) {
            Timber.d("stripe error charge capture %s",  e.getLocalizedMessage());
        } catch (APIException e) {
            Timber.d("stripe error charge capture %s",  e.getLocalizedMessage());
        } catch (CardException e) {
            Timber.d("stripe error charge capture %s",  e.getLocalizedMessage());
        }
        return result;
    }

    private String transferAmount(Map<String, Object> transferParams)
    {
        String result = null;
        try {
            Transfer transfer1 = Transfer.create(mMapParams);
            Timber.d("stripe transfer %s", transfer1);
            result = transfer1.getId();
        }catch (APIConnectionException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (InvalidRequestException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (AuthenticationException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (APIException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (CardException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        }
        return result;
    }

    private String refundAmount(Map<String, Object> refundParams)
    {
        String result = null;
        try {
            Refund refund = Refund.create(refundParams);
            Timber.d("stripe refund %s", refund);
            result = refund.getId();
        }catch (APIConnectionException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (InvalidRequestException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (AuthenticationException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (APIException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
        } catch (CardException e) {
            Timber.d("stripe error creating transfer %s",  e.getLocalizedMessage());
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
