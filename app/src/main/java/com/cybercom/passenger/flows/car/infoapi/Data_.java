package com.cybercom.passenger.flows.car.infoapi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data_ {

    @SerializedName("make")
    @Expose
    private String make;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("vehicle_year")
    @Expose
    private Integer vehicleYear;
    @SerializedName("model_year")
    @Expose
    private Integer modelYear;

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(Integer vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public Data_(String make, String model, Integer status, String color, String type, Integer vehicleYear, Integer modelYear) {
        this.make = make;
        this.model = model;
        this.status = status;
        this.color = color;
        this.type = type;
        this.vehicleYear = vehicleYear;
        this.modelYear = modelYear;
    }
}
