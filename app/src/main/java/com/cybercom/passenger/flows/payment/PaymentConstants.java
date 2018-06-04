package com.cybercom.passenger.flows.payment;

public class PaymentConstants {
    public static final String STRIPE_API_KEY = "sk_test_hOEYoyhsiD3MOJiWW33ztX48";
    public static final String STRIPE_API_KEY_PUBLISHABLE = "pk_test_QM2w0W5t19PihSq8BMXkSXMY";
    //public static final String IP = "10.90.192.123";
    public static final String BANK_ACCOUNT = "000123456789";
    public static final String CUSTOM_ACCOUNT = "custom";
    public static final String SWEDEN = "SE";
    public static final String CITY = "MALMO";
    public static final String ADDRESS_LINE1 = "MINC";
    public static final String POSTAL_CODE = "21119";
    public static final String INDIVIDUAL = "individual";
    public static final String PRODUCT_DESCRIPTION = "test";
    public static final String CURRENCY = "usd";
    public static final double BASE_SINGLE_PRICE = 35.0; //35kr per swedish mile (0 to 4 miles)
    public static final double RISE_SINGLE_PRICE = 15.0;  //15 per swedish mile after 4 miles
    public static final double BASE_MULTIPLE_PRICE = 50.0; //35kr + 15kr per swedish mile (0 to 4 miles)
    public static final double RISE_MULTIPLE_PRICE = 30.0; //15  + 15kr per swedish mile after 4 miles
    public static final double TOP_UP_MULTIPLE_PRICE = 10.0; //10kr extra for multiple person
    public static final double BASE_PRICE = 35.0; //35kr is base fare

}
