package com.cybercom.passenger.flows.payment;

        import android.os.AsyncTask;
        import com.stripe.Stripe;
        import com.stripe.model.Account;
        import java.util.HashMap;
        import java.util.Map;
        import timber.log.Timber;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.ADDRESS_LINE1;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.CITY;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.CUSTOM_ACCOUNT;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.INDIVIDUAL;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.POSTAL_CODE;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.PRODUCT_DESCRIPTION;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.SWEDEN;

public class StripeAccountAsyncTask extends AsyncTask<String, Void, String> {

    private int mDay, mMonth, mYear;
    private String mFirstName;
    private String mLastName;
    private String mTokenId;
    private String mEmail;
    private String mIpAddress;

    private OnAccountCreated mAccountDelegate;

    public interface OnAccountCreated{
        void updateAccountId(String accountId);
    }

    public StripeAccountAsyncTask(int day, int month, int year, String firstName, String lastName,
                                  String tokenId, String email, String ipAddress, OnAccountCreated onAccountCreated){
        mDay = day;
        mMonth = month;
        mYear = year;
        mFirstName = firstName;
        mLastName = lastName;
        mTokenId = tokenId;
        mEmail = email;
        mIpAddress = ipAddress;
        mAccountDelegate = onAccountCreated;
    }

    @Override
    protected String doInBackground(String... strings) {
        String accountId = postData(mDay, mMonth, mYear, mFirstName, mLastName, mTokenId, mEmail, mIpAddress);
        Timber.d("account id%s", accountId);
        return accountId;
    }

    @Override
    protected void onPostExecute(String accountId) {
        super.onPostExecute(accountId);
        Timber.d(" account created %s", accountId);
        mAccountDelegate.updateAccountId(accountId);
    }


    private String postData(int day, int month, int year, String firstName, String lastName, String tokenId, String email, String ipAddress) {

        Stripe.apiKey = STRIPE_API_KEY;

        try {
            //for creating account
            Map<String, Object> accountParams = new HashMap<>();
            accountParams.put("type", CUSTOM_ACCOUNT);
            accountParams.put("country", SWEDEN);
            com.stripe.model.Account account = Account.create(accountParams);
            Timber.d(account.getId()+"key="+account.getKeys().getPublishable()+"secrete="+account.getKeys().getSecret());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("internal_id", "44");
            Map<String, Object> params = new HashMap<>();
            params.put("metadata", metadata);
            account.update(params);

            Map<String, Object> emailParams = new HashMap<>();
            params.put("email", email);
            account.update(emailParams);

            Map<String, Object> tosAcceptanceParams = new HashMap<>();
            tosAcceptanceParams.put("date", System.currentTimeMillis() / 1000L);
            tosAcceptanceParams.put("ip", ipAddress); // Assumes you're not using a proxy
            Map<String, Object> paramsw = new HashMap<>();
            paramsw.put("tos_acceptance", tosAcceptanceParams);

            Map<String, Object> dobParams = new HashMap<>();
            dobParams.put("day", day);
            dobParams.put("month", month);
            dobParams.put("year", year);
            Map<String, Object> addressParams = new HashMap<>();
            addressParams.put("city", CITY);
            addressParams.put("line1", ADDRESS_LINE1);
            addressParams.put("postal_code", POSTAL_CODE);
            Map<String, Object> personalAddressParams = new HashMap<>();
            personalAddressParams.put("city", CITY);
            personalAddressParams.put("line1", ADDRESS_LINE1);
            personalAddressParams.put("postal_code", POSTAL_CODE);

            Map<String, Object> legalEntityParams = new HashMap<>();
            legalEntityParams.put("address", addressParams);
            legalEntityParams.put("personal_address", personalAddressParams);
            legalEntityParams.put("dob", dobParams);
            legalEntityParams.put("first_name", firstName);
            legalEntityParams.put("last_name", lastName);
            legalEntityParams.put("type", INDIVIDUAL);
            paramsw.put("product_description", PRODUCT_DESCRIPTION);

            paramsw.put("legal_entity", legalEntityParams);
            account.update(paramsw);

            Map<String, Object> external_account_params = new HashMap<>();
            external_account_params.put("external_account", tokenId);
            account.getExternalAccounts().create(external_account_params);

            return account.getId();

        }catch(Exception e){
            Timber.d("exception creating account %s", e.getLocalizedMessage());
        }
        return null;
    }


}