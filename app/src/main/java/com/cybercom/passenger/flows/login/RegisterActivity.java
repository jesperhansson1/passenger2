package com.cybercom.passenger.flows.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.SignUpActivity;
import com.cybercom.passenger.network.RetrofitHelper;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    String mType, mPassenger, mDriver, mLogin, mRegister;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.driver_section).setOnClickListener(this);
        findViewById(R.id.passenger_section).setOnClickListener(this);
        findViewById(R.id.button_register_login).setOnClickListener(this);
        mProgressBar = findViewById(R.id.progressBar2);
        mProgressBar.setVisibility(View.INVISIBLE);

        mPassenger = getResources().getString(R.string.signup_passenger);
        mDriver = getResources().getString(R.string.signup_driver);
        mLogin = getResources().getString(R.string.signup_login);
        mRegister = getResources().getString(R.string.signup_type);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.passenger_section:
                mType = mPassenger;
                startActivityNext(SignUpActivity.class);
                break;
            case R.id.driver_section:
                mType = mDriver;

                mProgressBar.setVisibility(View.VISIBLE);
                RetrofitHelper.getInstance(getApplicationContext(), null, null);

//                startActivityNext(SignUpActivity.class);
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
