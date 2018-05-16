package com.cybercom.passenger.flows.signup;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.cybercom.passenger.repository.PassengerRepository;

public class SignUpViewModel extends AndroidViewModel {
   // private FirebaseAuth mAuth;
    private PassengerRepository repository = PassengerRepository.getInstance();

    private static final String ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD";
    private static final String ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE";
    private static final String ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL";

    public SignUpViewModel(@NonNull Application application) {
        super(application);
     //   mAuth = FirebaseAuth.getInstance();
    }

   /* LiveData<FirebaseUser> createUserWithEmailAndPassword(String email, String password, final Activity activity){
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
                            Timber.w("createUserWithEmail:failure %s", ((FirebaseAuthException)task.getException()).getErrorCode()/*task.getException().getMessage().toString());
                        /*    if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_WEAK_PASSWORD){
                                SignUpActivity.mPassword.setError(task.getException().getMessage().toString());

                            }else if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_EMAIL_ALREADY_IN_USE){
                                SignUpActivity.mEmail.setError(task.getException().getMessage().toString());
                            }
                            else if(((FirebaseAuthException)task.getException()).getErrorCode() == ERROR_INVALID_EMAIL){
                                Timber.d("Error %s", ((FirebaseAuthException)task.getException()).getErrorCode());

                                SignUpActivity.mEmail.setError(task.getException().getMessage().toString());
                            }
                        }
                    }
                });
        return userMutableLiveData;
    }*/

    /*public void createUser(String userId, User user){
        repository.createUser(userId, user);
    }*/

    public LiveData<Boolean> validateEmail(String email){
        return repository.validateEmail(email);
    }
        /*mAuth.fetchSignInMethodsForEmail("e@e.se").addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getSignInMethods().size() > 0){
                        Timber.d("Email exists");
                    } else{
                        Timber.d("Email DOESNT exist");
                    }
                }
            }
        });

        return mAuth.fetchSignInMethodsForEmail("e@e.se");

    }*/
}
