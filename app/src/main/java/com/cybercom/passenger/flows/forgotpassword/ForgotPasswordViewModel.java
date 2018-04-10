package com.cybercom.passenger.flows.forgotpassword;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;

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

    public LiveData<Boolean> getNewPassword(String email){
        final MutableLiveData<Boolean> emailSent = new MutableLiveData<>();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                emailSent.setValue(true);
                            } else {
                                Timber.d("Email wasn't sent.");
                            }
                        }
                    });
        return emailSent;
    }
}
