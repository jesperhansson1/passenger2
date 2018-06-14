package com.cybercom.passenger.flows.payment;

import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.FileUpload;

import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeFileUploadAsyncTask extends AsyncTask<String, Void, String> {

    private Map<String, Object> mMapParams;

    private onUploadFile mOnUploadFileDelegate;

    public interface onUploadFile {
        void onFileUploaded(String fileUploadId);
    }

    public StripeFileUploadAsyncTask(Map<String, Object> mapParams, onUploadFile delegate) {
        mMapParams = mapParams;
        mOnUploadFileDelegate = delegate;
    }

    protected String doInBackground(String... params) {
        try {
            Stripe.apiKey = STRIPE_API_KEY;
            FileUpload fileUpload = FileUpload.create(mMapParams);
            return fileUpload.getId();
        } catch (APIConnectionException | InvalidRequestException | APIException |
                AuthenticationException | CardException e) {
            Timber.d("error uploading license file %s", e.getLocalizedMessage());
        }
        return null;
    }

    protected void onPostExecute(String fileUploadId) {
        Timber.d("file is uploaded %s", fileUploadId);
        if(mOnUploadFileDelegate != null) {
            mOnUploadFileDelegate.onFileUploaded(fileUploadId);
        }
        else {
            Timber.d("Failed to upload file.");
        }
    }
}



