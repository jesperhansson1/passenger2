package com.cybercom.passenger.flows.accounts;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.os.Environment;

import com.cybercom.passenger.repository.PassengerRepository;
import com.squareup.picasso.Picasso;
import com.stripe.Stripe;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.TokenCallback;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.model.FileUpload;
import com.stripe.model.Token;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.IP;
import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;

public class StripeAccount extends AsyncTask<String, Void, String> {
    PassengerRepository mPassengerRepository;
    String mUserId, mUserName, mBankNo;

    public StripeAccount(PassengerRepository passengerRepository){
        mPassengerRepository = passengerRepository;
    }

    public StripeAccount(PassengerRepository passengerRepository, String uId, String uName, String bankNo){
        mPassengerRepository = passengerRepository;
        mUserId = uId;
        mUserName = uName;
        mBankNo = bankNo;
    }

    @Override
    protected String doInBackground(String... strings) {
        return postData();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println(" done " +s);
        mPassengerRepository.updateAccId(mUserId,s);



    }


    public String postData() {

        Stripe.apiKey = STRIPE_API_KEY;

        try {
            //for creating account
            Map<String, Object> accountParams = new HashMap<String, Object>();
            accountParams.put("type", "custom");
            accountParams.put("country", "SE");
            com.stripe.model.Account ab = Account.create(accountParams);
            System.out.println(ab.getId()+"key="+ab.getKeys().getPublishable()+"secrete="+ab.getKeys().getSecret());

            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("internal_id", "44");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("metadata", metadata);
            ab.update(params);

            Map<String, Object> tosAcceptanceParams = new HashMap<String, Object>();
            tosAcceptanceParams.put("date", (long) System.currentTimeMillis() / 1000L);
            tosAcceptanceParams.put("ip", IP); // Assumes you're not using a proxy
            Map<String, Object> paramsw = new HashMap<String, Object>();
            paramsw.put("tos_acceptance", tosAcceptanceParams);
           // ab.update(paramsw);



            Map<String, Object> dobParams = new HashMap<String, Object>();
            dobParams.put("day", 1);
            dobParams.put("month", 1);
            dobParams.put("year", 2000);
            Map<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("city", "Malmö");
            addressParams.put("line1", "Minc");
            addressParams.put("postal_code", "21119");
            Map<String, Object> personalAddressParams = new HashMap<String, Object>();
            personalAddressParams.put("city", "Malmö");
            personalAddressParams.put("line1", "Minc");
            personalAddressParams.put("postal_code", "21119");

            Map<String, Object> legalEntityParams = new HashMap<String, Object>();
            legalEntityParams.put("address", addressParams);
            legalEntityParams.put("personal_address", personalAddressParams);
            legalEntityParams.put("dob", dobParams);
            legalEntityParams.put("first_name", "Tom");
            legalEntityParams.put("last_name", "Jerry");
            legalEntityParams.put("type", "individual");
            paramsw.put("product_description", "test");

            paramsw.put("legal_entity", legalEntityParams);
            ab.update(paramsw);

            Map<String, Object> params1 = new HashMap<String, Object>();
            params1.put("external_account", "tok_visa_debit");
            ab.getExternalAccounts().create(params1);

         /*   File file = new File("https://firebasestorage.googleapis.com/v0/b/passenger-e970d.appspot.com/o/download.jpeg?alt=media&token=8b96463d-5e9e-4154-8625-f7e2483bb0cb");
            Map<String, Object> fileuploadParams = new HashMap<String, Object>();
            fileuploadParams.put("document", "identity_document");
            fileuploadParams.put("file", file);
            FileUpload fu=FileUpload.create(fileuploadParams);

            Map<String, Object> verificationParams = new HashMap<String, Object>();
            verificationParams.put("document",fu.getId());
            paramsw.put("verification",verificationParams);*/




            /*
            "external_account",
      "legal_entity.address.city",
      "legal_entity.address.line1",
      "legal_entity.address.postal_code",
      "legal_entity.dob.day",
      "legal_entity.dob.month",
      "legal_entity.dob.year",
      "legal_entity.first_name",
      "legal_entity.last_name",
      "legal_entity.personal_address.city",
      "legal_entity.personal_address.line1",
      "legal_entity.personal_address.postal_code",
      "legal_entity.type",
      "product_description",
      "tos_acceptance.date",
      "tos_acceptance.ip"
             */

            //acceptance of connected accounts
            //updating bank details
           /* Map<String, Object> tosAcceptanceParams = new HashMap<String, Object>();
            tosAcceptanceParams.put("date", (long) System.currentTimeMillis() / 1000L);
            tosAcceptanceParams.put("ip", IP); // Assumes you're not using a proxy
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("tos_acceptance", tosAcceptanceParams);
            ab.update(params);


            Map<String, Object> tokenParams = new HashMap<String, Object>();
            Map<String, Object> bankAccountParams = new HashMap<String, Object>();
            bankAccountParams.put("country", "SE");
            bankAccountParams.put("currency", "sek");
            bankAccountParams.put("account_holder_name", mUserName);
            bankAccountParams.put("account_number", mBankNo);
            tokenParams.put("bank_account", bankAccountParams);
            Token bankToken = Token.create(tokenParams);
            Map<String, Object> paramse = new HashMap<String, Object>();
            paramse.put("external_account", bankToken.getId());
            ab.update(paramse);*/
            return ab.getId();

        }catch(Exception e){
            System.out.println("here " + e.getLocalizedMessage());
        }
        return null;
    }


}
