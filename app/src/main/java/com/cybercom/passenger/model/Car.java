package com.cybercom.passenger.model;
//Adapter class for car
public class Car {
    private String mNumber;
    private String mModel;
    private String mYear;
    private String mColor;
   /* private String mCarId;
    private String mUserId;*/

    public Car(String number, String model, String year, String colour){//}, String carId, String userId) {
        mNumber = number;
        mModel = model;
        mYear = year;
        mColor = colour;
        /*mCarId = carId;
        mUserId = userId;*/
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

   /* public String getCarId() {
        return mCarId;
    }

    public void setCarId(String carId) {
        mCarId = carId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }*/

    @Override
    public String toString() {
        return "Car{" +
                "mNumber='" + mNumber + '\'' +
                ", mModel='" + mModel + '\'' +
                ", mYear=" + mYear +
                ", mColor='" + mColor + /*'\'' +
                ", mCarId='" + mCarId + '\'' +
                ", mUserId='" + mUserId + '\'' +*/
                '}';
    }
}
