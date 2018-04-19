package com.cybercom.passenger.utils;

public class CarNumberHelper {

    public static String getKeyFromNumber(String carNumber)
    {
        return carNumber.replaceAll(" ", "_");
    }

    public static String getNumberFromKey(String carKey)
    {
        return carKey.replaceAll("_", " ");
    }
}
