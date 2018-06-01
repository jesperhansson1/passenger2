package com.cybercom.passenger.flows.payment;

        import android.os.AsyncTask;
        import com.stripe.Stripe;
        import com.stripe.exception.APIConnectionException;
        import com.stripe.exception.APIException;
        import com.stripe.exception.AuthenticationException;
        import com.stripe.exception.CardException;
        import com.stripe.exception.InvalidRequestException;
        import com.stripe.model.Customer;
        import com.stripe.model.Token;
        import com.stripe.android.model.Card;
        import java.util.HashMap;
        import java.util.Map;
        import timber.log.Timber;

        import static com.cybercom.passenger.model.ConstantValues.STRIPE_API_KEY;

public class StripeToken extends AsyncTask<String, Void, Token> {

    com.stripe.android.model.Card mCard = null;
    Token mToken = null;

    public StripeToken(Card card) {
        mCard = card;
        Timber.d(mCard.toString());
    }

    public StripeToken() {

    }

    @Override
    protected Token doInBackground(String... params) {
        mToken =  postData(mCard);
        Timber.d("Token id" + mToken.getId());
        return mToken;
    }

    @Override
    protected void onPostExecute(Token token) {
        super.onPostExecute(token);
        Timber.d("token created " + token.toString());
        StripeCustomer stripeCustomer = (StripeCustomer) new StripeCustomer(token).execute();
    }

    public Token postData(Card card) {
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
            mToken = Token.create(tokenParams);
            Timber.d("token created " + mToken.toString());
            return mToken;

        }
        catch(Exception e)
        {
            Timber.e("error creating token " + e.getMessage());
        }
        return mToken;
    }
}


