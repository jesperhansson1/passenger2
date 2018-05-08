package com.cybercom.passenger.flows.payment;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.accounts.StripeAccount;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.RideFare;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.maps.model.LatLng;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.stripe.model.Account;
import com.stripe.model.FileUpload;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.APP_SHARE;
import static com.cybercom.passenger.model.ConstantValues.DRIVER_SHARE;
import static com.cybercom.passenger.model.ConstantValues.PRICE;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;

public class PaymentActivity extends AppCompatActivity {

    CardInputWidget mCardInputWidget;
    Card mCardToSave;
    LifecycleOwner myLife;
    PaymentViewModel mPaymentViewModel;
    TextView mTextViewCharge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

    myLife = this;
        mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget_payment);
        mTextViewCharge = findViewById(R.id.textView_payment_distanceamount);
        findViewById(R.id.button_payment_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });
        findViewById(R.id.button_payment_calculate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePayment();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // createCustomer();
                Toast.makeText(getApplicationContext(),"creating customer " ,Toast.LENGTH_LONG).show();
               // uploadFile();
            }
        });
        mPaymentViewModel = ViewModelProviders.of(this).get(PaymentViewModel.class);
    }

    public void calculatePayment()
    {

        Notification not = PassengerRepository.getInstance().currentNotificationValue();
        double startLong = not.getDriveRequest().getStartLocation().getLongitude();
        double startLati = not.getDriveRequest().getStartLocation().getLatitude();
        double endLong = not.getDriveRequest().getEndLocation().getLongitude();
        double endLati = not.getDriveRequest().getEndLocation().getLatitude();


        LatLng start = new LatLng(55.604981,13.003822);
        LatLng end = new LatLng(56.046467,12.694512);

        start = new LatLng(startLati,startLong);
        end = new LatLng(endLati,endLong);
        mPaymentViewModel.getRideFare(start,end).observe(myLife, new Observer<RideFare>() {

            @Override
            public void onChanged(@Nullable RideFare rideFare) {
                Timber.d("Distance :" + rideFare.getDistance());
                Timber.d("Duration :" + rideFare.getDuration());
                Timber.d("Destination :" + rideFare.getEndLocation());
                Timber.d("Source :" + rideFare.getStartLocation());

                mTextViewCharge.setText(getCharge(rideFare.getDistance()));
            }
        });

    }

    /*public String getCharge(String dist)
    {
        double dd = Double.valueOf(dist)*PRICE/10;
        return String.valueOf(Math.round(dd));
    }*/

    public double mDriverAmount = 0.0,mAppAmount=0.0,mTotalAmount=0.0;

    public String getCharge(String dist)
    {
        //distance is in meters
        //meter to km
        System.out.println("distance is " + dist + " in meter ");
        System.out.println("distance is " + Double.valueOf(dist)/1000 + " in km ");
        mTotalAmount = (Double.valueOf(dist)/1000)*PRICE;
        mAppAmount = (Double.valueOf(dist)/1000)*APP_SHARE;
        mDriverAmount = (Double.valueOf(dist)/1000)*DRIVER_SHARE;

        System.out.println("Total amount " + (Double.valueOf(dist)/1000)*PRICE + " 35kr per 10 km ");
        System.out.println("Total amount " + (Double.valueOf(dist)/1000)*DRIVER_SHARE + " 18.5kr per 10 km driver  ");
        System.out.println("Total amount " + (Double.valueOf(dist)/1000)*APP_SHARE + " 16.5kr per 10 km pass");

        double dd = Double.valueOf(dist)*PRICE/10;
        return String.valueOf(Math.round(dd));
    }


    public void startPayment()
    {
        mCardToSave = mCardInputWidget.getCard();

        if (mCardToSave == null) {
            showToast("Invalid Card Data");
        }
        else
        {
            if (!mCardToSave.validateCard()) {
                // Do not continue token creation.
                showToast("Invalid Card Data");
            }
            else
            {
                // Do not continue token creation.
                showToast("valid Card Data");
                payC();
            }
        }
    }

    public void showToast(String errString)
    {
        Toast.makeText(getApplicationContext(),errString,Toast.LENGTH_LONG).show();
    }

    public void payC()
    {
        PaymentConfiguration.init(STRIPE_API_KEY);
        new Stripe(getApplicationContext()).createToken(
                mCardToSave,
                PaymentConfiguration.getInstance().getPublishableKey(),
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        Timber.d("token " + token);
                      //  new StripeCharge(token.getId(),"test",mTextViewCharge.getText().toString()).execute();
                        new StripeCharge(token.getId(),"test",mDriverAmount,mTotalAmount).execute();
                    }
                    public void onError(Exception error) {
                        Timber.e("error " + error.getLocalizedMessage());
                    }
                });
    }


    public void createCustomer()
    {
        PassengerRepository repository = PassengerRepository.getInstance();

        new StripeAccount(repository).execute();
    }

     public void uploadFile()
     {
  //       acct_1CPFefBuow8HQwvZ
       new UploadF().execute();
     }

    public class UploadF extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            new Thread() {
                @Override
                public void run() {
                    postData();

                }
            }.start();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Timber.d(s);
        }

        public void postData()
        {
            com.stripe.Stripe.apiKey = STRIPE_API_KEY;
            try
            {
                System.out.println("Path="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                System.out.println("AbsolutrPathPath="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
                System.out.println("AbsoluteFle="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsoluteFile().getName());

                //File f = (new FileCache(getApplicationContext())).getFile("https://firebasestorage.googleapis.com/v0/b/passenger-e970d.appspot.com/o/download.jpeg?alt=media&token=8b96463d-5e9e-4154-8625-f7e2483bb0cb");
//                File fl = new File(new URI("https://firebasestorage.googleapis.com/v0/b/passenger-e970d.appspot.com/o/download.jpeg?alt=media&token=8b96463d-5e9e-4154-8625-f7e2483bb0cb"));

                URI uri = new URI("https://firebasestorage.googleapis.com/v0/b/passenger-e970d.appspot.com/o/download.jpeg?alt=media&token=8b96463d-5e9e-4154-8625-f7e2483bb0cb");
                File auxFile = new File(uri.toString());

                Map<String, Object> fileuploadParams = new HashMap<String, Object>();
                fileuploadParams.put("purpose", "identity_document");

                //fileuploadParams.put("document", "identity_document");
               // File f=new File("/storage/emulated/0/Download/th.jpg")
               // File f=new File("/storage/emulated/0/Download/th.jpg")
                fileuploadParams.put("file", auxFile);

                FileUpload fu = FileUpload.create(fileuploadParams);
                System.out.println(fu.getId()+"--"+fu.getPurpose());

                Map<String, Object> verificationParams = new HashMap<String, Object>();
                verificationParams.put("document",fu.getId());
//         paramsw.put("verification",verificationParams);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }

    }
}
