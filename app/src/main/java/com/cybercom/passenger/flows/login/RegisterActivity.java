package com.cybercom.passenger.flows.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.SignUpActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    String mType, mPassenger, mDriver, mLogin, mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.imageButton_register_driver).setOnClickListener(this);
        findViewById(R.id.imageButton_register_passenger).setOnClickListener(this);
        findViewById(R.id.button_register_login).setOnClickListener(this);
        mPassenger = getResources().getString(R.string.signup_passenger);
        mDriver = getResources().getString(R.string.signup_driver);
        mLogin = getResources().getString(R.string.signup_login);
        mRegister = getResources().getString(R.string.signup_type);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton_register_passenger:
                mType = mPassenger;
                startActivityNext(SignUpActivity.class);
                break;
            case R.id.imageButton_register_driver:
                mType = mDriver;
                startActivityNext(SignUpActivity.class);
                break;
            case R.id.button_register_login:
                mType = mLogin;
                startActivityNext(LoginActivity.class);
                break;
        }
    }

    public void startActivityNext(Class target){
        Intent newIntent = new Intent(getApplicationContext(),target);
        newIntent.putExtra(mRegister,mType);
        startActivity(newIntent);
    }
}
