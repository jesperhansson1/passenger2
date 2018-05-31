package com.cybercom.passenger.flows.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.forgotpassword.ForgotPasswordActivity;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.utils.ToastHelper;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    private Button mSignup;
    private Button mForgotPassword;
    private Intent mIntent;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.login_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlue));


        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mEmail = findViewById(R.id.edittext_loginscreen_email);
        mPassword = findViewById(R.id.edittext_loginscreen_password);
        mLogin = findViewById(R.id.button_loginscreen_login);
        mLogin.setOnClickListener(this);
        mSignup = findViewById(R.id.button_loginscreen_signup);
        mSignup.setOnClickListener(this);
        mForgotPassword = findViewById(R.id.button_loginscreen_forgotpassword);
        mForgotPassword.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
        mLogin.setText(R.string.login);
    }

    @Override
    public void onClick(View v) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        mAuth = FirebaseAuth.getInstance();

        switch (v.getId()) {
            case R.id.button_loginscreen_login:
                if (email.isEmpty() || password.isEmpty()) {
                    ToastHelper.makeToast(getResources().getString(R.string.toast_enter_email_password), LoginActivity.this).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mLogin.setText("");
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    mIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(mIntent);
                                } else {
                                    mProgressBar.setVisibility(View.GONE);
                                    mLogin.setText(R.string.login);
                                    mEmail.setError(getResources().getString(
                                            R.string.toast_incorrect_email_password));
                                }
                            });
                }
                break;
            case R.id.button_loginscreen_signup:
                        mIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(mIntent);
                        break;
                    case R.id.button_loginscreen_forgotpassword:
                        mIntent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                        startActivity(mIntent);
                        break;
        }
    }
}
