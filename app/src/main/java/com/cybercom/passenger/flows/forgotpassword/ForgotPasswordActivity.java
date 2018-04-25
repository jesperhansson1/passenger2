package com.cybercom.passenger.flows.forgotpassword;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.PasswordSentActivity;
import com.cybercom.passenger.utils.ToastHelper;


public class ForgotPasswordActivity extends AppCompatActivity{
    EditText mResetPasswordMail;
    Button mResetPasswordButton;
    ForgotPasswordViewModel mViewModel;
    public ProgressBar progressBar;

    public static final String EXTRA_SESSION_EMAIL = "EXTRA_SESSION_EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.forgot_password_title);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        mViewModel = ViewModelProviders.of(this).get(ForgotPasswordViewModel.class);

        mResetPasswordMail = findViewById(R.id.edittext_forgotpassword_mail);
        mResetPasswordButton = findViewById(R.id.button_forgotpassword_sendmepassword);

        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mResetPasswordMail.getText().toString();
                getNewPassword(email);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        mResetPasswordButton.setText(R.string.send_me_password);
    }

    public void getNewPassword(final String email){
        if(!email.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);
            mResetPasswordButton.setText("");

            mViewModel.getNewPassword(email.trim(), this).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean result) {
                    if(result) {
                        Intent intent = new Intent(getApplicationContext(), PasswordSentActivity.class);
                        intent.putExtra(EXTRA_SESSION_EMAIL, email);
                        startActivity(intent);
                    } else{
                        progressBar.setVisibility(View.GONE);
                        mResetPasswordButton.setText(R.string.send_me_password);
                    }
                }
            });
        } else{
            ToastHelper.makeToast(getResources().getString(R.string.toast_send_forgot_password_email), this).show();
        }
    }
}
