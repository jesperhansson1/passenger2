package com.cybercom.passenger.model.Customer;

public class Legal_entity
{
    private String business_name;

    private String first_name;

    private Verification verification;

    private Address address;

    private Dob dob;

    private Personal_address personal_address;

    private String last_name;

    private String business_tax_id_provided;

    private String type;

    private String[] additional_owners;

    public String getBusiness_name ()
{
    return business_name;
}

    public void setBusiness_name (String business_name)
    {
        this.business_name = business_name;
    }

    public String getFirst_name ()
{
    return first_name;
}

    public void setFirst_name (String first_name)
    {
        this.first_name = first_name;
    }

    public Verification getVerification ()
    {
        return verification;
    }

    public void setVerification (Verification verification)
    {
        this.verification = verification;
    }

    public Address getAddress ()
    {
        return address;
    }

    public void setAddress (Address address)
    {
        this.address = address;
    }

    public Dob getDob ()
    {
        return dob;
    }

    public void setDob (Dob dob)
    {
        this.dob = dob;
    }

    public Personal_address getPersonal_address ()
    {
        return personal_address;
    }

    public void setPersonal_address (Personal_address personal_address)
    {
        this.personal_address = personal_address;
    }

    public String getLast_name ()
{
    return last_name;
}

    public void setLast_name (String last_name)
    {
        this.last_name = last_name;
    }

    public String getBusiness_tax_id_provided ()
    {
        return business_tax_id_provided;
    }

    public void setBusiness_tax_id_provided (String business_tax_id_provided)
    {
        this.business_tax_id_provided = business_tax_id_provided;
    }

    public String getType ()
{
    return type;
}

    public void setType (String type)
    {
        this.type = type;
    }

    public String[] getAdditional_owners ()
    {
        return additional_owners;
    }

    public void setAdditional_owners (String[] additional_owners)
    {
        this.additional_owners = additional_owners;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [business_name = "+business_name+", first_name = "+first_name+", verification = "+verification+", address = "+address+", dob = "+dob+", personal_address = "+personal_address+", last_name = "+last_name+", business_tax_id_provided = "+business_tax_id_provided+", type = "+type+", additional_owners = "+additional_owners+"]";
    }
}
