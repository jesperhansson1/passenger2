package com.cybercom.passenger.network.model;

public class AuthResponse
{
    private String autoStartToken;

    private String orderRef;

    public String getAutoStartToken ()
    {
        return autoStartToken;
    }

    public void setAutoStartToken (String autoStartToken)
    {
        this.autoStartToken = autoStartToken;
    }

    public String getOrderRef ()
    {
        return orderRef;
    }

    public void setOrderRef (String orderRef)
    {
        this.orderRef = orderRef;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [autoStartToken = "+autoStartToken+", orderRef = "+orderRef+"]";
    }
}