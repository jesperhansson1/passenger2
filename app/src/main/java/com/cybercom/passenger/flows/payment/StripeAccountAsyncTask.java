package com.cybercom.passenger.flows.payment;

        import android.os.AsyncTask;

        import com.cybercom.passenger.repository.PassengerRepository;
        import com.stripe.Stripe;
        import com.stripe.model.Account;

        import java.util.HashMap;
        import java.util.Map;

        import timber.log.Timber;

        import static com.cybercom.passenger.flows.payment.PaymentConstants.ADDRESS_LINE1;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.CITY;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.CUSTOM_ACCOUNT;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.INDIVIDUAL;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.IP;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.POSTAL_CODE;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.PRODUCT_DESCRIPTION;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.SWEDEN;

public class StripeAccountAsyncTask extends AsyncTask<String, Void, String> {

    int mDay, mMonth, mYear;
    String mFirstName, mLastName, mTokenId, mEmail, mAccountId;

    OnAccountCreated mAccountDelegate;

    public interface OnAccountCreated{
        void updateAccountId(String accountId);
    }

    public StripeAccountAsyncTask(int day, int month, int year, String firstName, String lastName, String tokenId, String email, OnAccountCreated onAccountCreated){
        mDay = day;
        mMonth = month;
        mYear = year;
        mFirstName = firstName;
        mLastName = lastName;
        mTokenId = tokenId;
        mEmail = email;
        mAccountDelegate = onAccountCreated;
    }

    @Override
    protected String doInBackground(String... strings) {
        mAccountId =  postData(mDay, mMonth, mYear, mFirstName, mLastName, mTokenId, mEmail);
        Timber.d("account id" + mAccountId.toString());
        return mAccountId;
    }

    @Override
    protected void onPostExecute(String accountId) {
        super.onPostExecute(accountId);
        Timber.d(" account created " + accountId);
        mAccountDelegate.updateAccountId(accountId);
    }


    public String postData(int day, int month, int year, String firstName, String lastName, String tokenId, String email) {

        Stripe.apiKey = STRIPE_API_KEY;

        try {
            //for creating account
            Map<String, Object> accountParams = new HashMap<String, Object>();
            accountParams.put("type", CUSTOM_ACCOUNT);
            accountParams.put("country", SWEDEN);
            com.stripe.model.Account account = Account.create(accountParams);
            Timber.d(account.getId()+"key="+account.getKeys().getPublishable()+"secrete="+account.getKeys().getSecret());

            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("internal_id", "44");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("metadata", metadata);
            account.update(params);

            Map<String, Object> emailParams = new HashMap<String, Object>();
            params.put("email", email);
            account.update(emailParams);

           /* Map<String, Object> currencyParams = new HashMap<String, Object>();
            params.put("default_currency", CURRENCY);
            account.update(currencyParams);*/


            Map<String, Object> tosAcceptanceParams = new HashMap<String, Object>();
            tosAcceptanceParams.put("date", (long) System.currentTimeMillis() / 1000L);
            tosAcceptanceParams.put("ip", IP); // Assumes you're not using a proxy
            Map<String, Object> paramsw = new HashMap<String, Object>();
            paramsw.put("tos_acceptance", tosAcceptanceParams);

            Map<String, Object> dobParams = new HashMap<String, Object>();
            dobParams.put("day", day);
            dobParams.put("month", month);
            dobParams.put("year", year);
            Map<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("city", CITY);
            addressParams.put("line1", ADDRESS_LINE1);
            addressParams.put("postal_code", POSTAL_CODE);
            Map<String, Object> personalAddressParams = new HashMap<String, Object>();
            personalAddressParams.put("city", CITY);
            personalAddressParams.put("line1", ADDRESS_LINE1);
            personalAddressParams.put("postal_code", POSTAL_CODE);

            Map<String, Object> legalEntityParams = new HashMap<String, Object>();
            legalEntityParams.put("address", addressParams);
            legalEntityParams.put("personal_address", personalAddressParams);
            legalEntityParams.put("dob", dobParams);
            legalEntityParams.put("first_name", firstName);
            legalEntityParams.put("last_name", lastName);
            legalEntityParams.put("type", INDIVIDUAL);
            paramsw.put("product_description", PRODUCT_DESCRIPTION);

            paramsw.put("legal_entity", legalEntityParams);
            account.update(paramsw);

            Map<String, Object> external_account_params = new HashMap<String, Object>();
            external_account_params.put("external_account", tokenId);
            account.getExternalAccounts().create(external_account_params);

            return account.getId();

        }catch(Exception e){
            Timber.d("exception creating account " + e.getLocalizedMessage());
        }
        return null;
    }


}