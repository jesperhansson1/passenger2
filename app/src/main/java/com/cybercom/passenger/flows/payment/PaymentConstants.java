package com.cybercom.passenger.flows.payment;

public class PaymentConstants {
    static final String STRIPE_API_KEY = "sk_test_hOEYoyhsiD3MOJiWW33ztX48";
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
    static final String CURRENCY_SEK = "sek";
    public static final String INTERNAL_ID = "44";
    static final double BASE_PRICE = 35.0; //35kr is base fare (0 to 4 miles)
    static final double RISE_PRICE = 15.0; //15kr is rise fare (4+ miles)
    static final double RISE_DOUBLE_PRICE = 15.0; //add 15Kr to end of price //if two people
    static final double RISE_TRIPLE_PRICE = 25.0; //add 15Kr + 10Kr to end of price
    static final double RISE_QUADRUPLE_PRICE = 35.0; //add 15Kr + 10Kr + 10Kr to end of price

    public static final String GOOGLE_API_ERROR = "Google api error";
    public static final String CARD_ERROR = "Error charging card";

    public static final int NOSHOW_FEE = 1850; //18.5kr to connected account


    static final String IDENTITY_DOCUMENT = "identity_document";

    //For card and token
    private static final String CARD_NUMBER = "number";
    private static final String CARD_EXP_MONTH = "exp_month";
    private static final String CARD_EXP_YEAR = "exp_year";
    private static final String CARD_CVC = "cvc";
    private static final String CARD_CURRENCY = "currency";
    static final String CARD = "card";
    static final String[] CARD_ARRAY = {CARD_NUMBER, CARD_EXP_MONTH, CARD_EXP_YEAR, CARD_CVC, CARD_CURRENCY};

    //For capture //pre blocking of amount
    private static final String CHARGE_AMOUNT = "amount";
    private static final String CHARGE_CURRENCY = "currency";
    private static final String CAPTURE = "capture";
    private static final String CHARGE_CUSTOMER = "customer";
    static final String[] CHARGE_ARRAY = {CHARGE_AMOUNT, CHARGE_CURRENCY, CAPTURE, CHARGE_CUSTOMER};

    //For transfer of amount to driver connected account from passenger customer account
    private static final String TRANSFER_AMOUNT = "amount";
    private static final String TRANSFER_CURRENCY = "currency";
    static final String TRANSFER_SOURCE_TRANSACTION = "source_transaction";
    private static final String TRANSFER_DESTINATION = "destination";
    static final String[] TRANSFER_ARRAY = {TRANSFER_AMOUNT, TRANSFER_CURRENCY, TRANSFER_SOURCE_TRANSACTION, TRANSFER_DESTINATION};

    //For refund - no show or any other to passenger customer account from passenger application
    private static final String REFUND_CHARGE = "charge";
    static final String[] REFUND_ARRAY = {REFUND_CHARGE};

    //For uploading legal document - license or passport for driver connected account
    private static final String UPLOAD_FILE = "file";
    private static final String UPLOAD_PURPOSE = "purpose";
    static final String[] UPLOAD_ARRAY = {UPLOAD_FILE, UPLOAD_PURPOSE};

    //For creating passenger customer account
    private static final String CUSTOMER_EMAIL = "email";
    private static final String CUSTOMER_SOURCE = "source";
    static final String[] CUSTOMER_ARRAY = {CUSTOMER_EMAIL, CUSTOMER_SOURCE};

    //For creating connected account
    private static final String CONNECT_TYPE = "type";
    private static final String CONNECT_COUNTRY = "country";
    private static final String CONNECT_INTERNAL_ID = "internal_id";
    private static final String CONNECT_METADATA = "metadata";
    private static final String CONNECT_EMAIL = "email";
    private static final String CONNECT_DATE = "date";
    private static final String CONNECT_IP = "ip";
    static final String CONNECT_TOS_ACCEPTANCE = "tos_acceptance";
    static final String CONNECT_DAY = "day";
    static final String CONNECT_MONTH = "month";
    static final String CONNECT_YEAR = "year";
    static final String CONNECT_CITY = "city";
    static final String CONNECT_LINE1 = "line1";
    static final String CONNECT_POSTAL_CODE = "postal_code";
    static final String CONNECT_ADDRESS = "address";
    static final String CONNECT_PERSONAL_ADDRESS = "personal_address";
    static final String CONNECT_DOB = "dob";
    static final String CONNECT_FIRST_NAME = "first_name";
    static final String CONNECT_LAST_NAME = "last_name";
    static final String CONNECT_DOCUMENT = "document";
    static final String CONNECT_VERIFICATION = "verification";
    static final String CONNECT_PRODUCT_DESCTIPTION = "product_description";
    static final String CONNECT_LEGAL_ENTITY = "legal_entity";
    static final String CONNECT_EXTERNAL_ACCOUNT = "external_account";

    static final String[] CONNECT_DOB_ARRAY = {CONNECT_DAY, CONNECT_MONTH, CONNECT_YEAR};
    static final String[] CONNECT_ADDRESS_ARRAY = {CONNECT_CITY, CONNECT_LINE1, CONNECT_POSTAL_CODE};
    static final String[] CONNECT_PERSONAL_ADDRESS_ARRAY = {CONNECT_CITY, CONNECT_LINE1, CONNECT_POSTAL_CODE};
    static final String[] CONNECT_VERIFICATION_ARRAY = {CONNECT_DOCUMENT};
    static final String[] CONNECT_LEGALENTITY_ARRAY = {CONNECT_ADDRESS, CONNECT_PERSONAL_ADDRESS, CONNECT_DOB,
            CONNECT_FIRST_NAME, CONNECT_LAST_NAME, CONNECT_TYPE, CONNECT_VERIFICATION};
    static final String[] CONNECT_TOSACCEPTANCE_ARRAY = {CONNECT_DATE, CONNECT_IP};
    static final String[] CONNECT_METADATA_ARRAY = {CONNECT_INTERNAL_ID};
    static final String[] CONNECT_ACCOUNT_ARRAY = {CONNECT_TYPE, CONNECT_COUNTRY, CONNECT_METADATA, CONNECT_EMAIL,
            CONNECT_TOS_ACCEPTANCE, CONNECT_PRODUCT_DESCTIPTION, CONNECT_LEGAL_ENTITY};

}
