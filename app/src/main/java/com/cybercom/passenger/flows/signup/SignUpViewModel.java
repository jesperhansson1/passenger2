package com.cybercom.passenger.flows.signup;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;

public class SignUpViewModel extends AndroidViewModel {
    PassengerRepository repository = PassengerRepository.getInstance();

    public SignUpViewModel(@NonNull Application application) {
        super(application);
    }

    public void createUser(String userId, User user){
        repository.createUser(userId, user);
    }
}
