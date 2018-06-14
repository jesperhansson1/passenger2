package com.cybercom.passenger.flows.payment;

        import android.os.AsyncTask;
        import com.stripe.Stripe;
        import com.stripe.exception.APIConnectionException;
        import com.stripe.exception.APIException;
        import com.stripe.exception.AuthenticationException;
        import com.stripe.exception.CardException;
        import com.stripe.exception.InvalidRequestException;
        import com.stripe.model.Account;
        import java.util.HashMap;
        import java.util.Map;
        import timber.log.Timber;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_EXTERNAL_ACCOUNT;
        import static com.cybercom.passenger.flows.payment.PaymentConstants.STRIPE_API_KEY;

public class StripeAccountAsyncTask extends AsyncTask<String, Void, String> {

    private OnAccountCreated mAccountDelegate;
    private Map<String, Object> mMapParams;
    private String mTokenId;

    public interface OnAccountCreated{
        void updateAccountId(String accountId);
    }

    public StripeAccountAsyncTask(Map<String, Object> mapParams, String tokenId, OnAccountCreated onAccountCreated){

        mAccountDelegate = onAccountCreated;
        mTokenId = tokenId;
        mMapParams = mapParams;
    }

    @Override
    protected String doInBackground(String... strings) {
        Stripe.apiKey = STRIPE_API_KEY;
        try {
            Account account = Account.create(mMapParams);
            Timber.d(account.getId()+"key="+account.getKeys().getPublishable()+"secrete="+account.getKeys().getSecret());

            Map<String, Object> external_account_params = new HashMap<>();
            external_account_params.put(CONNECT_EXTERNAL_ACCOUNT, mTokenId);
            account.getExternalAccounts().create(external_account_params);
            Timber.d("stripe account created %s", account);
            return account.getId();
        } catch (APIConnectionException | InvalidRequestException | APIException |
                AuthenticationException | CardException e) {
            Timber.d("stripe exception creating account %s", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String accountId) {
        Timber.d("stripe account created %s", accountId);
        mAccountDelegate.updateAccountId(accountId);
    }

}