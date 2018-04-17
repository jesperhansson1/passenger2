package com.cybercom.passenger.flows.accounts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
    }

    public void startBankActivity(View target){
        startActivity(new Intent(this, BankActivity.class));
    }

    public void startCardActivity(View target){
        startActivity(new Intent(this, CardActivity.class));
    }
}
