package com.cybercom.passenger.model.Customer;

public class Personal_address
{
    private String postal_code;

    private String state;

    private String line1;

    private String line2;

    private String country;

    private String city;

    public String getPostal_code ()
{
    return postal_code;
}

    public void setPostal_code (String postal_code)
    {
        this.postal_code = postal_code;
    }

    public String getState ()
{
    return state;
}

    public void setState (String state)
    {
        this.state = state;
    }

    public String getLine1 ()
{
    return line1;
}

    public void setLine1 (String line1)
    {
        this.line1 = line1;
    }

    public String getLine2 ()
{
    return line2;
}

    public void setLine2 (String line2)
    {
        this.line2 = line2;
    }

    public String getCountry ()
    {
        return country;
    }

    public void setCountry (String country)
    {
        this.country = country;
    }

    public String getCity ()
{
    return city;
}

    public void setCity (String city)
    {
        this.city = city;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [postal_code = "+postal_code+", state = "+state+", line1 = "+line1+", line2 = "+line2+", country = "+country+", city = "+city+"]";
    }
}
