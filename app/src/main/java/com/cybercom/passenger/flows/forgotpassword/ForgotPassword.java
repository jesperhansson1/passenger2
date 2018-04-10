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
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.login.Login;
import com.cybercom.passenger.flows.signup.PasswordSent;
import com.cybercom.passenger.flows.signup.SignUpViewModel;

import timber.log.Timber;

public class ForgotPassword extends AppCompatActivity{
    EditText mResetPasswordMail;
    Button mResetPasswordButton;
    Intent intent;
    ForgotPasswordViewModel mViewModel;
    String mEmail;

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

    public void getNewPassword(String email){
        mEmail = email.trim();
        if(!mEmail.isEmpty()){
            mViewModel.getNewPassword(mEmail, this).observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String result) {
                    if(!result.isEmpty()) {
                        intent = new Intent(getApplicationContext(), PasswordSent.class);
                        intent.putExtra("EXTRA_SESSION_EMAIL", mEmail);
                        startActivity(intent);
                    }
                }
            });
        } else{
            Toast.makeText(ForgotPassword.this, "You haven't entered any email", Toast.LENGTH_LONG).show();
            //Add ForgotPasswordActivity
        }
    }
}
