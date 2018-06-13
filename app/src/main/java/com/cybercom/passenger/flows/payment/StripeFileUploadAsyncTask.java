package com.cybercom.passenger.flows.payment;


import android.os.AsyncTask;

import com.stripe.Stripe;
import com.stripe.model.FileUpload;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.IDENTITY_DOCUMENT;
import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeFileUploadAsyncTask extends AsyncTask<String, Void, String> {

    private File mFile;

    private onUploadFile mOnUploadFileDelegate;

    public interface onUploadFile {
        void onFileUploaded(String fileUploadId);
    }

    StripeFileUploadAsyncTask(File file, onUploadFile delegate)
    {
        mFile = file;
        mOnUploadFileDelegate = delegate;
    }

    protected String doInBackground(String... params) {
        try
        {
            Stripe.apiKey = STRIPE_API_KEY;
            Map<String, Object> fileParams = new HashMap<>();
            fileParams.put("file", mFile);
            fileParams.put("purpose",IDENTITY_DOCUMENT);
            FileUpload fileUpload = FileUpload.create(fileParams);
            return fileUpload.getId();
        }
        catch (Exception e)
        {
            Timber.d("error uploading license file %s", e.getMessage());
            return null;
        }
    }

    protected void onPostExecute(String fileUploadId) {
        Timber.d("file is uploaded %s", fileUploadId);
        if(mOnUploadFileDelegate != null)
        {
            mOnUploadFileDelegate.onFileUploaded(fileUploadId);
        }
        else
        {
            Timber.d("Failed to upload file.");
        }
    }
}



