package com.cybercom.passenger.flows.signup;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class SignUpViewModel extends AndroidViewModel {
    FirebaseAuth mAuth;
    PassengerRepository repository = PassengerRepository.getInstance();

    public SignUpViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<FirebaseUser> createUserWithEmailAndPassword(String email, String password, Activity activity){
        final MutableLiveData<FirebaseUser> userMutableLiveData = new MutableLiveData<>();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity , new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getInstance().getCurrentUser();

                            Timber.d("createUserWithEmail:success %s", user);
                            userMutableLiveData.setValue(user);
                        } else {
                            Timber.w("createUserWithEmail:failure %s", task.getException());
                        }
                    }
                });
        return userMutableLiveData;
    }

    public void createUser(String userId, User user){
        Timber.d("USER creatUser in viewmodel");
        repository.createUser(userId, user);
    }
}
