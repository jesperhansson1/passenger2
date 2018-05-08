package com.cybercom.passenger.model.Customer;

public class MyPojo
{
    private String statement_descriptor;

    private String business_url;

    private String object;

    private String type;

    private String product_description;

    private Tos_acceptance tos_acceptance;

    private Decline_charge_on decline_charge_on;

    private String debit_negative_balances;

    private String id;

    private String details_submitted;

    private String payout_statement_descriptor;

    private String timezone;

    private String created;

    private String default_currency;

    private Payout_schedule payout_schedule;

    private String metadata;

    private Keys keys;

    private String payouts_enabled;

    private String charges_enabled;

    private String support_email;

    private String country;

    private String business_name;

    private String display_name;

    private Verification verification;

    private String email;

    private External_accounts external_accounts;

    private String support_phone;

    private Legal_entity legal_entity;

    public String getStatement_descriptor ()
    {
        return statement_descriptor;
    }

    public void setStatement_descriptor (String statement_descriptor)
    {
        this.statement_descriptor = statement_descriptor;
    }

    public String getBusiness_url ()
{
    return business_url;
}

    public void setBusiness_url (String business_url)
    {
        this.business_url = business_url;
    }

    public String getObject ()
    {
        return object;
    }

    public void setObject (String object)
    {
        this.object = object;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getProduct_description ()
{
    return product_description;
}

    public void setProduct_description (String product_description)
    {
        this.product_description = product_description;
    }

    public Tos_acceptance getTos_acceptance ()
    {
        return tos_acceptance;
    }

    public void setTos_acceptance (Tos_acceptance tos_acceptance)
    {
        this.tos_acceptance = tos_acceptance;
    }

    public Decline_charge_on getDecline_charge_on ()
    {
        return decline_charge_on;
    }

    public void setDecline_charge_on (Decline_charge_on decline_charge_on)
    {
        this.decline_charge_on = decline_charge_on;
    }

    public String getDebit_negative_balances ()
    {
        return debit_negative_balances;
    }

    public void setDebit_negative_balances (String debit_negative_balances)
    {
        this.debit_negative_balances = debit_negative_balances;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getDetails_submitted ()
    {
        return details_submitted;
    }

    public void setDetails_submitted (String details_submitted)
    {
        this.details_submitted = details_submitted;
    }

    public String getPayout_statement_descriptor ()
{
    return payout_statement_descriptor;
}

    public void setPayout_statement_descriptor (String payout_statement_descriptor)
    {
        this.payout_statement_descriptor = payout_statement_descriptor;
    }

    public String getTimezone ()
    {
        return timezone;
    }

    public void setTimezone (String timezone)
    {
        this.timezone = timezone;
    }

    public String getCreated ()
    {
        return created;
    }

    public void setCreated (String created)
    {
        this.created = created;
    }

    public String getDefault_currency ()
    {
        return default_currency;
    }

    public void setDefault_currency (String default_currency)
    {
        this.default_currency = default_currency;
    }

    public Payout_schedule getPayout_schedule ()
    {
        return payout_schedule;
    }

    public void setPayout_schedule (Payout_schedule payout_schedule)
    {
        this.payout_schedule = payout_schedule;
    }

    public String getMetadata ()
    {
        return metadata;
    }

    public void setMetadata (String metadata)
    {
        this.metadata = metadata;
    }

    public Keys getKeys ()
    {
        return keys;
    }

    public void setKeys (Keys keys)
    {
        this.keys = keys;
    }

    public String getPayouts_enabled ()
    {
        return payouts_enabled;
    }

    public void setPayouts_enabled (String payouts_enabled)
    {
        this.payouts_enabled = payouts_enabled;
    }

    public String getCharges_enabled ()
    {
        return charges_enabled;
    }

    public void setCharges_enabled (String charges_enabled)
    {
        this.charges_enabled = charges_enabled;
    }

    public String getSupport_email ()
{
    return support_email;
}

    public void setSupport_email (String support_email)
    {
        this.support_email = support_email;
    }

    public String getCountry ()
    {
        return country;
    }

    public void setCountry (String country)
    {
        this.country = country;
    }

    public String getBusiness_name ()
{
    return business_name;
}

    public void setBusiness_name (String business_name)
    {
        this.business_name = business_name;
    }

    public String getDisplay_name ()
{
    return display_name;
}

    public void setDisplay_name (String display_name)
    {
        this.display_name = display_name;
    }

    public Verification getVerification ()
    {
        return verification;
    }

    public void setVerification (Verification verification)
    {
        this.verification = verification;
    }

    public String getEmail ()
    {
        return email;
    }

    public void setEmail (String email)
    {
        this.email = email;
    }

    public External_accounts getExternal_accounts ()
    {
        return external_accounts;
    }

    public void setExternal_accounts (External_accounts external_accounts)
    {
        this.external_accounts = external_accounts;
    }

    public String getSupport_phone ()
{
    return support_phone;
}

    public void setSupport_phone (String support_phone)
    {
        this.support_phone = support_phone;
    }

    public Legal_entity getLegal_entity ()
    {
        return legal_entity;
    }

    public void setLegal_entity (Legal_entity legal_entity)
    {
        this.legal_entity = legal_entity;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [statement_descriptor = "+statement_descriptor+", business_url = "+business_url+", object = "+object+", type = "+type+", product_description = "+product_description+", tos_acceptance = "+tos_acceptance+", decline_charge_on = "+decline_charge_on+", debit_negative_balances = "+debit_negative_balances+", id = "+id+", details_submitted = "+details_submitted+", payout_statement_descriptor = "+payout_statement_descriptor+", timezone = "+timezone+", created = "+created+", default_currency = "+default_currency+", payout_schedule = "+payout_schedule+", metadata = "+metadata+", keys = "+keys+", payouts_enabled = "+payouts_enabled+", charges_enabled = "+charges_enabled+", support_email = "+support_email+", country = "+country+", business_name = "+business_name+", display_name = "+display_name+", verification = "+verification+", email = "+email+", external_accounts = "+external_accounts+", support_phone = "+support_phone+", legal_entity = "+legal_entity+"]";
    }
}
