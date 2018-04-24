package com.cybercom.passenger.flows.accounts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

import timber.log.Timber;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener{

    static final String BankCard = "CARDBANK";
    static final String Bank = "BANK";
    static final String Card = "CARD";
    static final String LOGINARRAY = "loginArray";
    static final String CARARRAY = "carArray";

    Bundle mExtras;
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
                cardIntent.putExtra(BankCard,Card);
                addIntentValues(cardIntent,Card);
                startActivity(cardIntent);
                break;
            case R.id.bank_account_section:
                Intent bankIntent =new Intent(this, AccountDetail.class);
                bankIntent.putExtra(BankCard,Bank);
                addIntentValues(bankIntent,Bank);
                startActivity(bankIntent);
                break;
        }
    }

    public void addIntentValues(Intent intent, String type)
    {
        if (mExtras != null) {
            intent.putExtra(BankCard,type);
            intent.putExtra(LOGINARRAY, mExtras.getString(LOGINARRAY));
            if(mExtras.getString(CARARRAY) != null)
            {
                intent.putExtra(CARARRAY, mExtras.getString(CARARRAY));
            }
        }
        else
        {
            Timber.e("Error getting values");
        }
    }
}
