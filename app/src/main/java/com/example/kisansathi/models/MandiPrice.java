package com.example.kisansathi.models;

import java.io.Serializable;

public class MandiPrice implements Serializable {
    private String recordId;
    private String cropName;
    private String state;
    private String district;
    private String mandiName;
    private double minPrice;
    private double maxPrice;
    private double modalPrice;
    private String unit;
    private String date;
    private String source;
    private boolean isSampleData;

    // Required zero-argument constructor for Firebase Realtime Database
    public MandiPrice() {
    }

    public MandiPrice(String recordId, String cropName, String state, String district,
                      String mandiName, double minPrice, double maxPrice, double modalPrice,
                      String unit, String date, String source, boolean isSampleData) {
        this.recordId = recordId;
        this.cropName = cropName;
        this.state = state;
        this.district = district;
        this.mandiName = mandiName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.modalPrice = modalPrice;
        this.unit = unit;
        this.date = date;
        this.source = source;
        this.isSampleData = isSampleData;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMandiName() {
        return mandiName;
    }

    public void setMandiName(String mandiName) {
        this.mandiName = mandiName;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getModalPrice() {
        return modalPrice;
    }

    public void setModalPrice(double modalPrice) {
        this.modalPrice = modalPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isSampleData() {
        return isSampleData;
    }

    public void setSampleData(boolean sampleData) {
        isSampleData = sampleData;
    }
}