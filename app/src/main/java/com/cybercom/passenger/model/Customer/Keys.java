package com.cybercom.passenger.model.Customer;
public class Keys
{
    private String secret;

    private String publishable;

    public String getSecret ()
    {
        return secret;
    }

    public void setSecret (String secret)
    {
        this.secret = secret;
    }

    public String getPublishable ()
    {
        return publishable;
    }

    public void setPublishable (String publishable)
    {
        this.publishable = publishable;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [secret = "+secret+", publishable = "+publishable+"]";
    }
}
