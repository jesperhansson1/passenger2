package com.cybercom.passenger.flows.signup;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class SignUpViewModel extends AndroidViewModel {
    PassengerRepository repository = PassengerRepository.getInstance();

    public SignUpViewModel(@NonNull Application application) {
        super(application);
    }

    public void createUser(String userId, User user){
        Timber.d("Next user from viewmodel %s", user);
        repository.createUser(userId, user);
    }
}
