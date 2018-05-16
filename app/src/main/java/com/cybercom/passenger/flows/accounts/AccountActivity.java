package com.cybercom.passenger.flows.accounts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

import timber.log.Timber;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener{

    static final String BANKCARD = "CARDBANK";
    static final String BANK = "BANK";
    static final String CARD = "CARD";
    static final String LOGINARRAY = "loginArray";
    static final String CARARRAY = "carArray";

    private Bundle mExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mExtras = getIntent().getExtras();

        findViewById(R.id.credit_card_section).setOnClickListener(this);
        findViewById(R.id.bank_account_section).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.credit_card_section:
                Intent cardIntent = new Intent(this, AccountDetail.class);
                cardIntent.putExtra(BANKCARD, CARD);
                addIntentValues(cardIntent, CARD);
                startActivity(cardIntent);
                break;
            case R.id.bank_account_section:
                Intent bankIntent =new Intent(this, AccountDetail.class);
                bankIntent.putExtra(BANKCARD, BANK);
                addIntentValues(bankIntent, BANK);
                startActivity(bankIntent);
                break;
        }
    }

    private void addIntentValues(Intent intent, String type) {
        if (mExtras != null) {
            intent.putExtra(BANKCARD, type);
            intent.putExtra(LOGINARRAY, mExtras.getString(LOGINARRAY));
            if (mExtras.getString(CARARRAY) != null) {
                intent.putExtra(CARARRAY, mExtras.getString(CARARRAY));
            }
        } else {
            Timber.e("Error getting values");
        }
    }
}
