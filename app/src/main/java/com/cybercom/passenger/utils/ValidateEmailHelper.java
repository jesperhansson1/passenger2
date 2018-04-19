package com.cybercom.passenger.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateEmailHelper {
    public static String isValidEmailAddress(String emailAddress) {
        String emailRegEx;
        Pattern pattern;
        // Regex for a valid email address
        emailRegEx = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";
        // Compare the regex with the email address
        pattern = Pattern.compile(emailRegEx);
        Matcher matcher = pattern.matcher(emailAddress);
        if (!matcher.find()) {
            return "The email is badly formatted";
        }
        return null;
    }
}
