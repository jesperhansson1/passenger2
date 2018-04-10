package com.cybercom.passenger.utils;


import com.cybercom.passenger.flows.signup.SignUpActivity;

public class CheckTextFieldHelper {
    public static Boolean checkTextFieldsHelper(String email, String password, String fullName, String personalNumber, String phone){
        if(!email.isEmpty() && !password.isEmpty() && !fullName.isEmpty() && !personalNumber.isEmpty() && !phone.isEmpty()){
            SignUpActivity.mFilledInTextFields = true;
        } else{
            SignUpActivity.mFilledInTextFields = false;
        }
        if(email.isEmpty()){
            SignUpActivity.mEmail.setError("Please enter an email");
        }
        if(password.isEmpty()){
            SignUpActivity.mPassword.setError("Please enter a password");
        }
        if(fullName.isEmpty()){
            SignUpActivity.mFullName.setError("Please enter your name");
        }
        if(personalNumber.isEmpty()){
            SignUpActivity.mPersonalNumber.setError("Please enter your personal number");
        }
        if(phone.isEmpty()){
            SignUpActivity.mPhone.setError("Please enter your phone number");
        }
        return SignUpActivity.mFilledInTextFields;
    }
}
