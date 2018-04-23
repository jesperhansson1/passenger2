package com.cybercom.passenger.flows.accounts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

public class AccountActivity extends AppCompatActivity {

    static final String BankCard = "CARDBANK";
    static final String Bank = "BANK";
    static final String Card = "CARD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
    }

    public void startBankActivity(View target){
        Intent bankIntent =new Intent(this, AccountDetail.class);
        bankIntent.putExtra(BankCard,Bank);
        startActivity(bankIntent);
    }

    public void startCardActivity(View target){
        Intent cardIntent =new Intent(this, AccountDetail.class);
        cardIntent.putExtra(BankCard,Card);
        startActivity(cardIntent);
    }
}
