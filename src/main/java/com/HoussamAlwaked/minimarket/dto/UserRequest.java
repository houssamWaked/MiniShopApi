package com.HoussamAlwaked.minimarket.dto;

import com.HoussamAlwaked.minimarket.entity.UserRole;

public class UserRequest {

    private String name;
    private String email;
    private UserRole role;
    private String assignedStoreId;

    public UserRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getAssignedStoreId() {
        return assignedStoreId;
    }

    public void setAssignedStoreId(String assignedStoreId) {
        this.assignedStoreId = assignedStoreId;
    }
}
