package com.cybercom.passenger.model.Customer;

public class Payout_schedule
{
    private String interval;

    private String delay_days;

    public String getInterval ()
    {
        return interval;
    }

    public void setInterval (String interval)
    {
        this.interval = interval;
    }


    public String getDelay_days ()
    {
        return delay_days;
    }

    public void setDelay_days (String delay_days)
    {
        this.delay_days = delay_days;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [interval = "+interval+", delay_days = "+delay_days+"]";
    }
}
