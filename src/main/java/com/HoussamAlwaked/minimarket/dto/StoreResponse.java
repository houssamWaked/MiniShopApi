package com.HoussamAlwaked.minimarket.dto;

import java.util.ArrayList;
import java.util.List;

public class StoreResponse {

    private String id;
    private String name;
    private String address;
    private String categoryId;
    private List<String> subAdminIds = new ArrayList<>();
    private boolean hasActiveOffer;

    public StoreResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getSubAdminIds() {
        return subAdminIds;
    }

    public void setSubAdminIds(List<String> subAdminIds) {
        this.subAdminIds = subAdminIds == null ? new ArrayList<>() : subAdminIds;
    }

    public boolean isHasActiveOffer() {
        return hasActiveOffer;
    }

    public void setHasActiveOffer(boolean hasActiveOffer) {
        this.hasActiveOffer = hasActiveOffer;
    }
}
