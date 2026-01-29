package com.HoussamAlwaked.minimarket.entity;

public class User {

    private String id;
    private String name;
    private String email;
    private UserRole role;
    private String assignedStoreId;

    public User() {
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
