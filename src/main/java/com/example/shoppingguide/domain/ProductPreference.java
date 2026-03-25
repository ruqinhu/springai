package com.example.shoppingguide.domain;

import java.util.List;

public class ProductPreference {

    private String category;
    private Double minPrice;
    private Double maxPrice;
    private List<String> keyFeatures;
    private String brandPreference;
    private String summary;

    public ProductPreference() {
    }

    public ProductPreference(String category, Double minPrice, Double maxPrice, List<String> keyFeatures, String brandPreference, String summary) {
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.keyFeatures = keyFeatures;
        this.brandPreference = brandPreference;
        this.summary = summary;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<String> getKeyFeatures() {
        return keyFeatures;
    }

    public void setKeyFeatures(List<String> keyFeatures) {
        this.keyFeatures = keyFeatures;
    }

    public String getBrandPreference() {
        return brandPreference;
    }

    public void setBrandPreference(String brandPreference) {
        this.brandPreference = brandPreference;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
