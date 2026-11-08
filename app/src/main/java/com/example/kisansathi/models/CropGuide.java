package com.example.kisansathi.models;

import java.io.Serializable;

public class CropGuide implements Serializable {
    private String cropId;
    private String cropName;
    private String season;
    private String soil;
    private String farmingSteps;
    private String irrigation;
    private String fertilizerGuidance;
    private String harvesting;
    private boolean isSampleData;

    // Required zero-argument constructor for Firebase Realtime Database
    public CropGuide() {
    }

    public CropGuide(String cropId, String cropName, String season, String soil,
                     String farmingSteps, String irrigation, String harvesting, boolean isSampleData) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.season = season;
        this.soil = soil;
        this.farmingSteps = farmingSteps;
        this.irrigation = irrigation;
        this.fertilizerGuidance = "";
        this.harvesting = harvesting;
        this.isSampleData = isSampleData;
    }

    public CropGuide(String cropId, String cropName, String season, String soil,
                     String farmingSteps, String irrigation, String fertilizerGuidance,
                     String harvesting, boolean isSampleData) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.season = season;
        this.soil = soil;
        this.farmingSteps = farmingSteps;
        this.irrigation = irrigation;
        this.fertilizerGuidance = fertilizerGuidance;
        this.harvesting = harvesting;
        this.isSampleData = isSampleData;
    }

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public String getFarmingSteps() {
        return farmingSteps;
    }

    public void setFarmingSteps(String farmingSteps) {
        this.farmingSteps = farmingSteps;
    }

    public String getIrrigation() {
        return irrigation;
    }

    public void setIrrigation(String irrigation) {
        this.irrigation = irrigation;
    }

    public String getFertilizerGuidance() {
        return fertilizerGuidance;
    }

    public void setFertilizerGuidance(String fertilizerGuidance) {
        this.fertilizerGuidance = fertilizerGuidance;
    }

    public String getHarvesting() {
        return harvesting;
    }

    public void setHarvesting(String harvesting) {
        this.harvesting = harvesting;
    }

    public boolean isSampleData() {
        return isSampleData;
    }

    public void setSampleData(boolean sampleData) {
        isSampleData = sampleData;
    }
}
