package com.cybercom.passenger.flows.payment;


import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.BASE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RISE_DOUBLE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RISE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RISE_QUADRUPLE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RISE_TRIPLE_PRICE;

public class CalculatePrice {

    private long mDistance;
    private int mNoOfSeats;

    public CalculatePrice(long distance, int noOfSeats) {
        mDistance = distance;
        mNoOfSeats = noOfSeats;
    }

    public long getDistance() {
        return mDistance;
    }

    public void setDistance(long distance) {

        mDistance = distance;
    }

    public int getNoOfSeats() {
        return mNoOfSeats;
    }

    public void setNoOfSeats(int noOfSeats) {
        mNoOfSeats = noOfSeats;
    }

    @Override
    public String toString() {
        return "CalculatePrice{" +
                "mDistance=" + mDistance +
                ", nNoOfSeats=" + mNoOfSeats +
                '}';
    }

    //Calculate total price for the given distance
    public double getPrice() {
        double price = 0.0;
        double distance = ((double)mDistance)/10000; //m to swedish mile
        if(distance <= 4.0) {
            price = distance *BASE_PRICE;
            Timber.d(" distance < 4 and 1 %s", price);
        }
        if(distance > 4.0) {
            price = 4.0 * BASE_PRICE + (distance - 4.0) * RISE_PRICE;
            Timber.d(" distance > 4 and 1 %s", price);
        }
        price = price + addPriceExtraPassenger();
        if(price < 35.0) {
            price = BASE_PRICE;
            Timber.d(" price is %s", price);
        }
        return price;
    }

    //Add rise price for total price depending on number of people
    private double addPriceExtraPassenger() {
        switch(mNoOfSeats) {
            case 2:
                return RISE_DOUBLE_PRICE;
            case 3:
                return RISE_TRIPLE_PRICE;
            case 4:
                return RISE_QUADRUPLE_PRICE;
            default:
                return 0.0;
        }
    }
}
