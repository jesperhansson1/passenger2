package com.cybercom.passenger.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("personalNumber")
    @Expose
    private String personalNumber;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("givenName")
    @Expose
    private String givenName;
    @SerializedName("surname")
    @Expose
    private String surname;

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

}