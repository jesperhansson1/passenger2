package com.cybercom.passenger.flows.forgotpassword;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cybercom.passenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import timber.log.Timber;

public class ForgotPassword extends AppCompatActivity{

    EditText mResetPasswordMail;
    Button mResetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        mResetPasswordMail = findViewById(R.id.edittext_forgotpassword_mail);
        mResetPasswordButton = findViewById(R.id.button_forgotpassword_sendmepassword);

        mResetPasswordMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = "user@example.com";

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Timber.d("Email sent.");
                                }
                            }
                        });    */
                Timber.d("Button clicked");
            }
        });
    }
}
