package com.HoussamAlwaked.minimarket.entity;

import java.util.ArrayList;
import java.util.List;

public class Store {

    private String id;
    private String name;
    private String address;
    private String categoryId;
    private List<String> subAdminIds = new ArrayList<>();

    public Store() {
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
}
