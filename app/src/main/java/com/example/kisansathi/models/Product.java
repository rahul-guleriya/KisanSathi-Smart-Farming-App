package com.example.kisansathi.models;

import java.io.Serializable;
import java.util.Locale;

public class Product implements Serializable {
    private String productId;
    private String name;
    private String category;
    private String company;
    private String packSize;
    private double numericQuantity;
    private String unit;
    private double price;
    private String updatedDate;
    private String usageInstructions;
    private String safetyGuidelines;
    private boolean isSampleData;

    // Required zero-argument constructor for Firebase Realtime Database
    public Product() {
    }

    public Product(String productId, String name, String category, String company,
                   String packSize, double numericQuantity, String unit, double price,
                   String updatedDate, String usageInstructions, String safetyGuidelines, boolean isSampleData) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.company = company;
        this.packSize = packSize;
        this.numericQuantity = numericQuantity;
        this.unit = unit;
        this.price = price;
        this.updatedDate = updatedDate;
        this.usageInstructions = usageInstructions;
        this.safetyGuidelines = safetyGuidelines;
        this.isSampleData = isSampleData;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPackSize() {
        return packSize;
    }

    public void setPackSize(String packSize) {
        this.packSize = packSize;
    }

    public double getNumericQuantity() {
        return numericQuantity;
    }

    public void setNumericQuantity(double numericQuantity) {
        this.numericQuantity = numericQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUsageInstructions() {
        return usageInstructions;
    }

    public void setUsageInstructions(String usageInstructions) {
        this.usageInstructions = usageInstructions;
    }

    public String getSafetyGuidelines() {
        return safetyGuidelines;
    }

    public void setSafetyGuidelines(String safetyGuidelines) {
        this.safetyGuidelines = safetyGuidelines;
    }

    public boolean isSampleData() {
        return isSampleData;
    }

    public void setSampleData(boolean sampleData) {
        isSampleData = sampleData;
    }

    public double getUnitPrice() {
        if (numericQuantity > 0) {
            return price / numericQuantity;
        }
        return 0.0;
    }

    public String getFormattedUnitPrice() {
        if (numericQuantity > 0 && unit != null) {
            return String.format(Locale.US, "₹%.2f / %s", getUnitPrice(), unit);
        }
        return "N/A";
    }
}
