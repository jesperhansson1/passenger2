package com.cybercom.passenger.flows.forgotpassword;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.signup.PasswordSentActivity;

import timber.log.Timber;

public class ForgotPasswordActivity extends AppCompatActivity{
    EditText mResetPasswordMail;
    Button mResetPasswordButton;
    ForgotPasswordViewModel mViewModel;
    public static final String EXTRA_SESSION_EMAIL = "EXTRA_SESSION_EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
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

    public void getNewPassword(final String email){
        if(!email.isEmpty()){
            mViewModel.getNewPassword(email.trim()).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean result) {
                    if(result) {
                        Intent intent = new Intent(getApplicationContext(), PasswordSentActivity.class);
                        intent.putExtra(EXTRA_SESSION_EMAIL, email);
                        startActivity(intent);
                    } else{
                        Timber.d("Email wasnt sent! Activity");
                    }
                }
            });
        } else{
            Timber.d("Email is empty");
        }
    }
}
