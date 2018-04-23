package com.cybercom.passenger.flows.accounts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.login.LoginActivity;
import com.cybercom.passenger.flows.signup.SignUpActivity;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener{

    static final String BankCard = "CARDBANK";
    static final String Bank = "BANK";
    static final String Card = "CARD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        findViewById(R.id.credit_card_section).setOnClickListener(this);
        findViewById(R.id.bank_account_section).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.credit_card_section:
                Intent cardIntent = new Intent(this, AccountDetail.class);
                cardIntent.putExtra(BankCard,Card);
                startActivity(cardIntent);
                break;
            case R.id.bank_account_section:
                Intent bankIntent =new Intent(this, AccountDetail.class);
                bankIntent.putExtra(BankCard,Bank);
                startActivity(bankIntent);
                break;
        }
    }
}
