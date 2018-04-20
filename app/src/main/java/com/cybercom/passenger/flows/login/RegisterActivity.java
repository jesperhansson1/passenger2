package com.cybercom.passenger.flows.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

import timber.log.Timber;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    static final String PASSENGER = "PASSENGER";
    static final String DRIVER = "DRIVER";
    String mType=PASSENGER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton_register_passenger:
                mType = PASSENGER;
                break;
            case R.id.imageButton_register_driver:
                mType = DRIVER;
                break;
            case R.id.button_register_login:
                Timber.d(mType);
                break;
        }
    }
}
