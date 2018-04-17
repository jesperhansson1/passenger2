package com.cybercom.passenger.model;

public class Car {
    private String mNumber;
    private String mModel;
    private String mYear;
    private String mColor;

    public Car(String number, String model, String year, String colour){//}, String carId, String userId) {
        mNumber = number;
        mModel = model;
        mYear = year;
        mColor = colour;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String colour) {
        mColor = colour;
    }

    @Override
    public String toString() {
        return "Car{" +
                "mNumber='" + mNumber + '\'' +
                ", mModel='" + mModel + '\'' +
                ", mYear=" + mYear + '\'' +
                ", mColor='" + mColor +
                '}';
    }
}
