package com.cybercom.passenger.model.Customer;

public class Dob
{
    private String month;

    private String year;

    private String day;

    public String getMonth ()
{
    return month;
}

    public void setMonth (String month)
    {
        this.month = month;
    }

    public String getYear ()
{
    return year;
}

    public void setYear (String year)
    {
        this.year = year;
    }

    public String getDay ()
{
    return day;
}

    public void setDay (String day)
    {
        this.day = day;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [month = "+month+", year = "+year+", day = "+day+"]";
    }
}

