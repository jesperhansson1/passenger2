package com.cybercom.passenger.model.Customer;

public class Tos_acceptance
{
    private String user_agent;

    private String date;

    private String ip;

    public String getUser_agent ()
{
    return user_agent;
}

    public void setUser_agent (String user_agent)
    {
        this.user_agent = user_agent;
    }

    public String getDate ()
{
    return date;
}

    public void setDate (String date)
    {
        this.date = date;
    }

    public String getIp ()
{
    return ip;
}

    public void setIp (String ip)
    {
        this.ip = ip;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [user_agent = "+user_agent+", date = "+date+", ip = "+ip+"]";
    }
}

