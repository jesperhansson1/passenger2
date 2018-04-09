package com.cybercom.passenger.flows.forgotpassword;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.cybercom.passenger.flows.signup.PasswordSent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import timber.log.Timber;

public class ForgotPasswordViewModel extends AndroidViewModel {
    private FirebaseAuth mAuth;

    public ForgotPasswordViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<String> getNewPassword(String email){
        final MutableLiveData<String> emailSent = new MutableLiveData<>();

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Timber.d("Email sent.");
                                emailSent.setValue("Email sent");
                            } else {
                                Timber.d("Email wasn't sent.");
                            }
                        }
                    });
        return emailSent;
    }
}
