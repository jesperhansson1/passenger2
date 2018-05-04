package com.cybercom.passenger.flows.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.SignUpActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    String mType, mPassenger, mDriver, mLogin, mRegister;
    Button mLoginButton;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.driver_section).setOnClickListener(this);
        findViewById(R.id.passenger_section).setOnClickListener(this);
        mLoginButton = findViewById(R.id.button_register_login);
        mLoginButton.setOnClickListener(this);
        mPassenger = getResources().getString(R.string.signup_passenger);
        mDriver = getResources().getString(R.string.signup_driver);
        mLogin = getResources().getString(R.string.signup_login);
        mRegister = getResources().getString(R.string.signup_type);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        mLoginButton.setText(R.string.login);
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
                startActivityNext(SignUpActivity.class);
                break;
            case R.id.button_register_login:
                progressBar.setVisibility(View.VISIBLE);
                mLoginButton.setText("");
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
