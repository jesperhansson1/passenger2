package com.cybercom.passenger.flows.accounts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

import timber.log.Timber;

public class AccountActivity extends AppCompatActivity {

    static final String BankCard = "CARDBANK";
    static final String Bank = "BANK";
    static final String Card = "CARD";
    Bundle mExtras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mExtras = getIntent().getExtras();

    }

    public void startBankActivity(View target){
        Intent bankIntent =new Intent(this, AccountDetail.class);
        //bankIntent.putExtra(BankCard,Bank);
        addIntentValues(bankIntent,Bank);
        startActivity(bankIntent);
    }

    public void startCardActivity(View target){
        Intent cardIntent =new Intent(this, AccountDetail.class);
        //cardIntent.putExtra(BankCard,Card);
        addIntentValues(cardIntent,Card);
        startActivity(cardIntent);
    }

    public void addIntentValues(Intent intent, String type)
    {
        if (mExtras != null) {
           // intent.putExtra("extraBundle",mExtras);
            intent.putExtra(BankCard,type);
            /*intent.putExtra("email",mExtras.getString("email"));
            intent.putExtra("password",mExtras.getString("password"));
            intent.putExtra("phone",mExtras.getString("phone"));
            intent.putExtra("personalnumber",mExtras.getString("personalnumber"));
            intent.putExtra("fullname",mExtras.getString("fullname"));
            intent.putExtra("gender", mExtras.getString("gender"));*/
            intent.putExtra("loginArray", mExtras.getStringArray("loginArray"));
            if((mExtras.getStringArray("carArray")!=null) &&
                    (mExtras.getStringArray("carArray").length >0))
            {
                intent.putExtra("carArray", mExtras.getStringArray("carArray"));
            }
        }
        else
        {
            Timber.e("Error getting values");
        }
    }
}
