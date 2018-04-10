package com.cybercom.passenger.flows.forgotpassword;

import android.app.Activity;
import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

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

    public LiveData<Boolean> getNewPassword(String email,  final Activity activity){
        final MutableLiveData<Boolean> emailSent = new MutableLiveData<>();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                emailSent.setValue(true);
                            } else {
                                Toast.makeText(activity, "You have entered an incorrect email", Toast.LENGTH_LONG).show();
                                //Add ForgotPasswordActivity
                            }
                        }
                    });
        return emailSent;
    }
}
