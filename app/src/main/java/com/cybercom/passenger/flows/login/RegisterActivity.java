package com.cybercom.passenger.flows.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.SignUpActivity;

import timber.log.Timber;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    String mType, PASSENGER, DRIVER, LOGIN, REGISTERTYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.driver_section).setOnClickListener(this);
        findViewById(R.id.passenger_section).setOnClickListener(this);
        findViewById(R.id.button_register_login).setOnClickListener(this);
        PASSENGER = getResources().getString(R.string.signup_passenger);
        DRIVER = getResources().getString(R.string.signup_driver);
        LOGIN = getResources().getString(R.string.signup_login);
        REGISTERTYPE = getResources().getString(R.string.signup_type);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.passenger_section:
                mType = PASSENGER;
                startActivityNext(SignUpActivity.class);
                break;
            case R.id.driver_section:
                mType = DRIVER;
                startActivityNext(SignUpActivity.class);
                break;
            case R.id.button_register_login:
                mType = LOGIN;
                startActivityNext(LoginActivity.class);
                break;
        }
    }

    public void startActivityNext(Class target){
        Intent newIntent = new Intent(getApplicationContext(),target);
        newIntent.putExtra(REGISTERTYPE,mType);
        startActivity(newIntent);
    }
}
