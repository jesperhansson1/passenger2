package com.cybercom.passenger.flows.payment;

        import android.os.AsyncTask;

        import com.cybercom.passenger.flows.accounts.CardFragment;
        import com.stripe.Stripe;
        import com.stripe.model.Token;
        import com.stripe.android.model.Card;
        import java.util.HashMap;
        import java.util.Map;
        import timber.log.Timber;

        import static com.cybercom.passenger.flows.payment.Constants.STRIPE_API_KEY;

public class StripeToken extends AsyncTask<String, Void, String> {

    com.stripe.android.model.Card mCard = null;
    String mToken = null;
    OnTokenCreated mTokenDelegate;

    public interface OnTokenCreated{
        void updateTokenId(String tokenId);
    }

    public StripeToken(Card card, OnTokenCreated caller) {
        mCard = card;
        mTokenDelegate = caller;
        Timber.d(mCard.toString());
    }

    @Override
    protected String doInBackground(String... params) {
        mToken =  postData(mCard);
        Timber.d("Token id" + mToken);
        return mToken;
    }

    @Override
    protected void onPostExecute(String token) {
        Timber.d("token created " + token.toString());
        mTokenDelegate.updateTokenId(token);
    }

    public String postData(Card card) {
        Token token = null;
        Stripe.apiKey = STRIPE_API_KEY;
        Map<String, Object> tokenParams = new HashMap<String, Object>();
        Map<String, Object> cardParams = new HashMap<String, Object>();

        cardParams.put("number", card.getNumber());
        cardParams.put("exp_month", card.getExpMonth());
        cardParams.put("exp_year", card.getExpYear());
        cardParams.put("cvc", card.getCVC());
        tokenParams.put("card", cardParams);

        try
        {
            token = Token.create(tokenParams);
            Timber.d("token created " + mToken.toString());
            return token.getId();

        }
        catch(Exception e)
        {
            Timber.e("error creating token " + e.getMessage());
        }
        return token.getId();
    }
}


