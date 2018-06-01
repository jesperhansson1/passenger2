package com.cybercom.passenger.flows.payment;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.payment.PriceDistance.ResultDistanceMatrix;
import com.cybercom.passenger.model.RideFare;
import com.google.android.gms.maps.model.LatLng;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.RequestOptions;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;

import com.stripe.android.view.CardInputWidget;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccountCollection;
import com.stripe.model.Token;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.APP_SHARE;
import static com.cybercom.passenger.model.ConstantValues.DRIVER_SHARE;
import static com.cybercom.passenger.model.ConstantValues.PRICE;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY1;

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
        mPaymentViewModel = ViewModelProviders.of(this).get(PaymentViewModel.class);
    }

    public void calculatePayment()
    {
        LatLng start = new LatLng(55.604981,13.003822);
        LatLng end = new LatLng(56.046467,12.694512);
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
       /* mCardToSave = mCardInputWidget.getCard();

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
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            //Your code goes here
                            creae();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

            }
        }*/
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                   StripeToken stripeToken = (StripeToken) new StripeToken(mCardInputWidget.getCard()).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void showToast(String errString)
    {
        Toast.makeText(getApplicationContext(),errString,Toast.LENGTH_LONG).show();
    }

    Customer customer;
    public void creae()
    {
        com.stripe.Stripe.apiKey = "sk_test_hOEYoyhsiD3MOJiWW33ztX48";//"pk_test_QM2w0W5t19PihSq8BMXkSXMY";

        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", "avc");
        //customerParams.put("default_card", mCardInputWidget.getCard());

        try {
            customer = Customer.create(customerParams);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("source", "tok_amex");
            customer.getSources().create(params);


            System.out.println("customer created " + customer.getId());
                               /* Map<String, Object> updateParams = new HashMap<String, Object>();
                                updateParams.put("source", token);
                                customer.update(updateParams);*/
            System.out.println("customer created " + customer.toString());

            Stripe stripe = new Stripe(getApplicationContext(), "pk_test_QM2w0W5t19PihSq8BMXkSXMY");

            stripe.createToken(
                    mCardInputWidget.getCard(),
                    new TokenCallback() {
                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(getApplicationContext(),
                                    error.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        @Override
                        public void onSuccess(com.stripe.android.model.Token token) {
                            Map<String, Object> sourcePparams = new HashMap<String, Object>();
                            System.out.println("token " + token.toString());
                            params.put("source", token);


                            try
                            {

                                customer.getSources().create(params);
                                System.out.println("customer created " + customer.toString());
                            }
                            catch(Exception e)
                            {
                                System.out.println(e.getMessage());
                            }




                        }

                    });






        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void createCustomer()
    {
        com.stripe.Stripe.apiKey = "sk_test_hOEYoyhsiD3MOJiWW33ztX48";//"pk_test_QM2w0W5t19PihSq8BMXkSXMY";
        //com.stripe.Stripe.apiKey = "pk_test_QM2w0W5t19PihSq8BMXkSXMY";

       /* Map<String, Object> tokenParams = new HashMap<String, Object>();
        Map<String, Object> cardParams = new HashMap<String, Object>();
        cardParams.put("number", mCardToSave.getNumber());
        cardParams.put("exp_month", mCardToSave.getExpMonth());
        cardParams.put("exp_year", mCardToSave.getExpYear());
        cardParams.put("cvc", mCardToSave.getCVC());
        tokenParams.put("card", cardParams);

        Token token = null;*/
        try
        {

            Stripe stripe = new Stripe(getApplicationContext(), "pk_test_QM2w0W5t19PihSq8BMXkSXMY");
            stripe.createToken(
                    mCardToSave,
                    new TokenCallback() {
                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(getApplicationContext(),
                                    error.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        @Override
                        public void onSuccess(com.stripe.android.model.Token token) {

                            System.out.println(token.getId());
                            Map<String, Object> customerParams = new HashMap<String, Object>();

                            Map<String, Object> shippingParams = new HashMap<String, Object>();
                            Map<String, Object> addressParams = new HashMap<String, Object>();
                            addressParams.put("line1", "test line 1");
                            shippingParams.put("address", addressParams);
                            shippingParams.put("name", "Test name1");

                          //  customerParams.put("shipping", shippingParams);

                            Map<String, Object> sourceParams = new HashMap<String, Object>();
                            sourceParams.put("object", "card");
                            sourceParams.put("number", "4242424242424242");
                            sourceParams.put("exp_month", 07);
                            sourceParams.put("exp_year", 2021);
                            sourceParams.put("cvc", 123);
                            sourceParams.put("currency", "usd");

                            //customerParams.put("source", sourceParams);

                            customerParams.put("description", "abc");

                            //customerParams.put("source", token);
                            try {
                                Customer customer = Customer.create(customerParams);
                                System.out.println("customer created " + customer.getId());
                               /* Map<String, Object> updateParams = new HashMap<String, Object>();
                                updateParams.put("source", token);
                                customer.update(updateParams);*/
                                System.out.println("customer created " + customer.toString());
                            }
                            catch(Exception e)
                            {
                                System.out.println(e.getMessage());
                            }

                        }
                    }
            );





          /*  token = Token.create(tokenParams);
            System.out.println("token " + token.getId());





            Map<String, Object> sourceParams = new HashMap<String, Object>();
            sourceParams.put("source", token.getId()); //?
            Card source = (Card) customer.getSources().create(sourceParams);



            Map<String, Object> params = new HashMap<String, Object>();
            params.put("source", token);
            customer.getSources().create(params);
            System.out.println("type " + customer.getSources().getRequestParams());*/
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
             e.printStackTrace();
        }


        //Stripe.apiKey = "pk_test_QM2w0W5t19PihSq8BMXkSXMY";



    }

   /* public void payC()
    {
        PaymentConfiguration.init(STRIPE_API_KEY1);
        new Stripe(getApplicationContext()).createToken(
                mCardToSave,
                PaymentConfiguration.getInstance().getPublishableKey(),
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        Timber.d("token " + token);
                       // new StripeCharge(token.getId(),"test",mTextViewCharge.getText().toString()).execute();
                        new StripeCharge(token.getId(),"test",mDriverAmount,mTotalAmount).execute();
                    }
                    public void onError(Exception error) {
                        Timber.e("error " + error.getLocalizedMessage());
                    }
                });
    }*/
}
