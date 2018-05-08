package com.cybercom.passenger.model.Customer;

public class Verification
{
    private String document;

    private String details;

    private String status;

    private String details_code;

    public String getDocument ()
{
    return document;
}

    public void setDocument (String document)
    {
        this.document = document;
    }

    public String getDetails ()
{
    return details;
}

    public void setDetails (String details)
    {
        this.details = details;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String getDetails_code ()
{
    return details_code;
}

    public void setDetails_code (String details_code)
    {
        this.details_code = details_code;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [document = "+document+", details = "+details+", status = "+status+", details_code = "+details_code+"]";
    }
}