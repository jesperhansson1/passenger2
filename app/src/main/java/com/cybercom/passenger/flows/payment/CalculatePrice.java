package com.cybercom.passenger.flows.payment;


import timber.log.Timber;

import static com.cybercom.passenger.flows.payment.PaymentConstants.BASE_MULTIPLE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.BASE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.BASE_SINGLE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RISE_MULTIPLE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.RISE_SINGLE_PRICE;
import static com.cybercom.passenger.flows.payment.PaymentConstants.TOP_UP_MULTIPLE_PRICE;

public class CalculatePrice {

    long mDistance;
    int mNoOfSeats;

    public CalculatePrice(long distance, int noOfSeats) {
        mDistance = distance;
        mNoOfSeats = noOfSeats;
    }

    public long getDistance() {
        return mDistance;
    }

    public void setmDistance(long distance) {
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

    public double getPrice()
    {
        double price = 0.0;
        double distance = ((double)mDistance)/10000; //m to swedish mile

        if(getNoOfSeats() == 1)
        {
            if(distance <= 4.0)
            {
                price = distance *BASE_SINGLE_PRICE;
                Timber.d(" distance < 4 and 1 " + price);
            }
            if(distance > 4.0)
            {
                price = 4.0 * BASE_SINGLE_PRICE + (distance - 4.0) * RISE_SINGLE_PRICE;
                Timber.d(" distance > 4 and 1 " + price);
            }
        }else
        {
            if(distance <= 4.0)
            {
                price = distance * BASE_MULTIPLE_PRICE;
                Timber.d(" distance < 4 and 2 " + price);
            }
            if(distance > 4.0)
            {
                price = 4.0 * BASE_MULTIPLE_PRICE + (distance - 4.0) * RISE_MULTIPLE_PRICE;
                Timber.d(" distance > 4 and 2 " + price);
            }
            price = price + TOP_UP_MULTIPLE_PRICE;
            Timber.d(" distance > 4 and 2 final " + price);
        }

        if(price < 35.0)
        {
            price = BASE_PRICE;
        }

        return price;
    }
}
