package com.cybercom.passenger.utils;

public class ValidatePersonalNumberHelper {
    public static boolean hasValidChecksum(String number) {
        if (number == null || "".equals(number.trim())) {
            return false;
        }

        // From http://sv.wikipedia.org/wiki/Luhn-algoritmen#C

        int a = 1;
        int sum = 0;
        int term;

        for (int i = number.length() - 1; i >= 0; i--) {
            term = Character.digit(number.charAt(i), 10) * a;
            if (term > 9) {
                term -= 9;
            }
            sum += term;
            a = 3 - a;
        }

        if ((sum % 10) == 0) {
            return true;
        }
        return false;
    }
}
