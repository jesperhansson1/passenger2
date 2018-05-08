package com.cybercom.passenger.model;

//To save constants used in this app

public class ConstantValues {

    // Payment status : 0 - success, 1 - pending, 2 - canceled, 18.5kr - price, sek - currency
    public static final int SUCCESS = 0;
    public static final int PENDING = 1;
    public static final int CANCELED = 2;
    public static final double PRICE = 350;//3.5Kr per km - 350 ore
    public static final double DRIVER_SHARE = 185;//18.5Kr per 10km - 185 ore
    public static final double APP_SHARE = 165;//16.5Kr per 10km - 165 ore
    public static final String CURRENCY = "SEK";
    public static final String UNIT = "metric";
    public static final String GOOGLE_API_BASE_URL = "http://maps.googleapis.com/";


    public static final String CAR_API_BASE_URL = "https://api.biluppgifter.se/api/v1/vehicle/regno/";
    public static final String CAR_API_KEY = "Dr1rKMm2eLgYKg4G1v1Xl0EEF30qeVHb9IZ5Ubji7HolwiCDwDfUZ2HVogo5";


    public static final String STRIPE_API_KEY = "sk_test_hOEYoyhsiD3MOJiWW33ztX48";
    public static final String STRIPE_API_KEY1 = "pk_test_QM2w0W5t19PihSq8BMXkSXMY";


}
