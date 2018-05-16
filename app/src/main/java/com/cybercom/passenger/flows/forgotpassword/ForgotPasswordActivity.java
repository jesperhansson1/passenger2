package com.cybercom.passenger.flows.forgotpassword;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.PasswordSentActivity;
import com.cybercom.passenger.utils.ToastHelper;


public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String EXTRA_SESSION_EMAIL = "EXTRA_SESSION_EMAIL";

    private EditText mResetPasswordMail;
    private Button mResetPasswordButton;
    private ForgotPasswordViewModel mViewModel;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.forgot_password_title);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mViewModel = ViewModelProviders.of(this).get(ForgotPasswordViewModel.class);

        mResetPasswordMail = findViewById(R.id.edittext_forgotpassword_mail);
        mResetPasswordButton = findViewById(R.id.button_forgotpassword_sendmepassword);

        mResetPasswordButton.setOnClickListener(v -> {
            String email = mResetPasswordMail.getText().toString();
            getNewPassword(email);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
        mResetPasswordButton.setText(R.string.send_me_password);
    }

    private void getNewPassword(final String email) {
        if(TextUtils.isEmpty(email)) {
            ToastHelper.makeToast(getResources().getString(
                    R.string.toast_send_forgot_password_email), this).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mResetPasswordButton.setText("");

        mViewModel.getNewPassword(email.trim(), this).observe(this, result -> {
            if(result) {
                Intent intent = new Intent(getApplicationContext(), PasswordSentActivity.class);
                intent.putExtra(EXTRA_SESSION_EMAIL, email);
                startActivity(intent);
            } else{
                mProgressBar.setVisibility(View.GONE);
                mResetPasswordButton.setText(R.string.send_me_password);
                mResetPasswordMail.setError(getResources().getString(
                        R.string.toast_forgot_password_incorrect_email));
            }
        });
    }
}
