package com.cybercom.passenger.flows.payment;


import com.stripe.android.model.Card;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.CARD;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CARD_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CHARGE_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_ACCOUNT_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_ADDRESS_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_DOB_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_LEGALENTITY_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_METADATA_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_PERSONAL_ADDRESS_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_TOSACCEPTANCE_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CONNECT_VERIFICATION_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CURRENCY_SEK;
import static com.cybercom.passenger.flows.payment.PaymentConstants.CUSTOMER_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.IDENTITY_DOCUMENT;
import static com.cybercom.passenger.flows.payment.PaymentConstants.REFUND_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TRANSFER_ARRAY;
import static com.cybercom.passenger.flows.payment.PaymentConstants.UPLOAD_ARRAY;

//Helper function for stripe
public class PaymentHelper {

//creates and returns hashmap with given keys and values
    private static Map<String, Object> createHashMap(Object[] values, String... keys) {
        Map<String, Object> mapParams = new HashMap<String, Object>();
        int i = 0;
        for (String key:keys) {
            mapParams.put(key, values[i]);
            Timber.d("stripe " + key + " " + values[i]);
            i++;
        }
        return mapParams;
    }

    //merges and returns hashmap with given key and value
    private static Map<String, Object> mergeHashMap(Map<String, Object> value, String key) {
        Map<String, Object> mapParams = new HashMap<String, Object>();
        mapParams.put(key, value);
        return mapParams;
    }

    //merges and returns hashmap with given keys and values
    public static Map<String, Object> mergeHashMapArray(Map<String, Object>[] values, String... keys) {
        Map<String, Object> mapParams = new HashMap<String, Object>();
        int i = 0;
        for (String key:keys) {
            mapParams.put(key, values[i]);
            Timber.d("stripe " + key + " " + values[i]);
            i++;
        }
        return mapParams;
    }

    //creates and returns hashmap with given keys and values
    private static Map<String, String> createHashMap(String[] values, String... keys) {
        Map<String, String> mapParams = new HashMap<String, String>();
        int i = 0;
        for (String key:keys) {
            mapParams.put(key, values[i]);
            Timber.d("stripe " + key + " " + values[i]);
            i++;
        }
        return mapParams;
    }

    //merges and returns hashmap for token object with given card values
    public static Map<String, Object> createTokenHashMap(Card card) {
        Object[] ojectArrayCard = new Object[5];
        ojectArrayCard[0] = card.getNumber();
        ojectArrayCard[1] = card.getExpMonth();
        ojectArrayCard[2] = card.getExpYear();
        ojectArrayCard[3] = card.getCVC();
        ojectArrayCard[4] = CURRENCY;//card.getCurrency();currently supported currency is usd
        return mergeHashMap(createHashMap(ojectArrayCard, CARD_ARRAY), CARD);
    }

    //creates and returns hashmap for pre-blocking amount on customer for a given card and amount
    public static Map<String, Object> createChargeHashMap(String customerId, int amount, boolean capture) {
        Object[] ojectArrayCharge = new Object[4];
        ojectArrayCharge[0] = amount;
        ojectArrayCharge[1] = CURRENCY_SEK;
        ojectArrayCharge[2] = capture;
        ojectArrayCharge[3] = customerId;
        return createHashMap(ojectArrayCharge, CHARGE_ARRAY);
    }

    //creates and returns hashmap for transfering amount from preblocked card to connected account
    public static Map<String, Object> createTransferHashMap(String chargeId, int amount, String accountId) {
        Object[] ojectArrayTransfer = new Object[4];
        ojectArrayTransfer[0] = amount;
        ojectArrayTransfer[1] = CURRENCY_SEK;
        ojectArrayTransfer[2] = chargeId;
        ojectArrayTransfer[3] = accountId;
        return createHashMap(ojectArrayTransfer, TRANSFER_ARRAY);
    }

    //creates and returns hashmap for refund from a given charge object
    public static Map<String, Object> createRefundHashMap(String chargeId) {
        Object[] ojectArrayRefund = new Object[1];
        ojectArrayRefund[0] = chargeId;
        return createHashMap(ojectArrayRefund, REFUND_ARRAY);
    }

    //creates and returns hashmap for uploading file from a given file
    public static Map<String, Object> createUploadFileHashMap(File file) {
        Object[] ojectArrayUpload = new Object[2];
        ojectArrayUpload[0] = file;
        ojectArrayUpload[1] = IDENTITY_DOCUMENT;
        return createHashMap(ojectArrayUpload, UPLOAD_ARRAY);
    }

    //creates and returns hashmap for customer object from email and given token
    public static Map<String, Object> createCustomerHashMap(String email, String tokenId) {
        Object[] ojectArrayCustomer = new Object[2];
        ojectArrayCustomer[0] = email;
        ojectArrayCustomer[1] = tokenId;
        return createHashMap(ojectArrayCustomer, CUSTOMER_ARRAY);
    }

    //creates and returns hashmap for dob object from given values
    private static Map<String, Object> createDobParamsHashMap(int day, int month, int year) {
        Object[] ojectArrayDob = new Object[3];
        ojectArrayDob[0] = day;
        ojectArrayDob[1] = month;
        ojectArrayDob[2] = year;
        return createHashMap(ojectArrayDob, CONNECT_DOB_ARRAY);
    }

    //creates and returns hashmap for address object from given values
    private static Map<String, Object> createAddressParamsHashMap(String city, String address_line1, String postal_code) {
        Object[] ojectArrayAddress = new Object[3];
        ojectArrayAddress[0] = city;
        ojectArrayAddress[1] = address_line1;
        ojectArrayAddress[2] = postal_code;
        return createHashMap(ojectArrayAddress, CONNECT_ADDRESS_ARRAY);
    }

    //creates and returns hashmap for personal address object from given values
    private static Map<String, Object> createPersonalAddressParamsHashMap(String city, String address_line1, String postal_code) {
        Object[] ojectArrayPersonalAddress = new Object[3];
        ojectArrayPersonalAddress[0] = city;
        ojectArrayPersonalAddress[1] = address_line1;
        ojectArrayPersonalAddress[2] = postal_code;
        return createHashMap(ojectArrayPersonalAddress, CONNECT_PERSONAL_ADDRESS_ARRAY);
    }

    //creates and returns hashmap for verification object from given values
    private static Map<String, String> createVerificationParamsHashMap(String legalDocumentId) {
        String[] stringArrayVerification = {legalDocumentId};
        return createHashMap(stringArrayVerification, CONNECT_VERIFICATION_ARRAY);
    }

    //creates and returns hashmap for tos acceptance object from given values
    public static Map<String, Object> createTosAcceptanceParamsHashMap(long time, String ipAddress) {
        Object[] ojectArrayTosAcceptance = new Object[2];
        ojectArrayTosAcceptance[0] = time;
        ojectArrayTosAcceptance[1] = ipAddress;
        return createHashMap(ojectArrayTosAcceptance, CONNECT_TOSACCEPTANCE_ARRAY);
    }

    //creates and returns hashmap for metadata object from given values
    public static Map<String, String> createMetaDataHashMap(String metaData) {
        String[] stringArrayMetaData = {metaData};
        return createHashMap(stringArrayMetaData, CONNECT_METADATA_ARRAY);
    }

    //creates and returns hashmap for legal entity object from given values
    public static Map<String, Object> createLegalEntityParamsHashMap(String city, String addressLine, String postalCode,
                                                                     int day, int month, int year, String firstName, String lastName,
                                                                     String type, String legalDocumentId) {
        Object[] ojectArrayLegalEntity = new Object[7];
        ojectArrayLegalEntity[0] = createAddressParamsHashMap(city, addressLine, postalCode);
        ojectArrayLegalEntity[1] = createPersonalAddressParamsHashMap(city, addressLine, postalCode);
        ojectArrayLegalEntity[2] = createDobParamsHashMap(day, month, year);
        ojectArrayLegalEntity[3] = firstName;
        ojectArrayLegalEntity[4] = lastName;
        ojectArrayLegalEntity[5] = type;
        ojectArrayLegalEntity[6] = createVerificationParamsHashMap(legalDocumentId);
        return createHashMap(ojectArrayLegalEntity, CONNECT_LEGALENTITY_ARRAY);
    }

    //creates and returns hashmap for account object from given values
    public static Map<String, Object> createAccountParamsHashMap(String type, String country, Map<String, String> metaData,
                                                                     String email, Map<String, Object> tosAcceptance,
                                                                 String productDescription, Map<String, Object> legalEntity) {
        Object[] ojectArrayLegalEntity = new Object[7];
        ojectArrayLegalEntity[0] = type;
        ojectArrayLegalEntity[1] = country;
        ojectArrayLegalEntity[2] = metaData;
        ojectArrayLegalEntity[3] = email;
        ojectArrayLegalEntity[4] = tosAcceptance;
        ojectArrayLegalEntity[5] = productDescription;
        ojectArrayLegalEntity[6] = legalEntity;
        return createHashMap(ojectArrayLegalEntity, CONNECT_ACCOUNT_ARRAY);
    }
}
