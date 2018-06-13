package com.cybercom.passenger.flows.payment;

public class PaymentConstants {
    public static final String STRIPE_API_KEY = "sk_test_hOEYoyhsiD3MOJiWW33ztX48";
    public static final String STRIPE_API_KEY_PUBLISHABLE = "pk_test_QM2w0W5t19PihSq8BMXkSXMY";
    public static final String BANK_ACCOUNT = "000123456789";
    public static final String CUSTOM_ACCOUNT = "custom";
    public static final String SWEDEN = "SE";
    public static final String CITY = "MALMO";
    public static final String ADDRESS_LINE1 = "MINC";
    public static final String POSTAL_CODE = "21119";
    public static final String INDIVIDUAL = "individual";
    public static final String PRODUCT_DESCRIPTION = "test";
    public static final String CURRENCY = "usd";
    public static final String CURRENCY_SEK = "sek";

    public static final double BASE_PRICE = 35.0; //35kr is base fare (0 to 4 miles)
    public static final double RISE_PRICE = 15.0; //15kr is rise fare (4+ miles)
    public static final double RISE_DOUBLE_PRICE = 15.0; //add 15Kr to end of price //if two people
    public static final double RISE_TRIPLE_PRICE = 25.0; //add 15Kr + 10Kr to end of price
    public static final double RISE_QUADRUPLE_PRICE = 35.0; //add 15Kr + 10Kr + 10Kr to end of price

    public static final String GOOGLE_API_ERROR = "Google api error";
    public static final String CARD_ERROR = "Error charging card";

    public static final int NOSHOW_FEE = 1850; //18.5kr to connected account


    public static final String IDENTITY_DOCUMENT = "identity_document";


}
