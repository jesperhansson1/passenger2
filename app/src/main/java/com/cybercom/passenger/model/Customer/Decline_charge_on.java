package com.cybercom.passenger.model.Customer;

public class Decline_charge_on
{
    private String avs_failure;

    private String cvc_failure;

    public String getAvs_failure ()
    {
        return avs_failure;
    }

    public void setAvs_failure (String avs_failure)
    {
        this.avs_failure = avs_failure;
    }

    public String getCvc_failure ()
    {
        return cvc_failure;
    }

    public void setCvc_failure (String cvc_failure)
    {
        this.cvc_failure = cvc_failure;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [avs_failure = "+avs_failure+", cvc_failure = "+cvc_failure+"]";
    }
}
