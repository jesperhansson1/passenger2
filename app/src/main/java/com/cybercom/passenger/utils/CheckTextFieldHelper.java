package com.cybercom.passenger.utils;

import com.cybercom.passenger.flows.signup.SignUp;

public class CheckTextFieldHelper {
    public static Boolean checkTextFieldsHelper(String email, String password, String fullName, String personalNumber, String phone){
        if(!email.isEmpty() && !password.isEmpty() && !fullName.isEmpty() && !personalNumber.isEmpty() && !phone.isEmpty()){
            SignUp.mFilledInTextFields = true;
        } else{
            SignUp.mFilledInTextFields = false;
        }
        if(email.isEmpty()){
            SignUp.mEmail.setError("Please enter an email");
        }
        if(password.isEmpty()){
            SignUp.mPassword.setError("Please enter a password");
        }
        if(fullName.isEmpty()){
            SignUp.mFullName.setError("Please enter your name");
        }
        if(personalNumber.isEmpty()){
            SignUp.mPersonalNumber.setError("Please enter your personal number");
        }
        if(phone.isEmpty()){
            SignUp.mPhone.setError("Please enter your phone number");
        }
        return SignUp.mFilledInTextFields;
    }
}
