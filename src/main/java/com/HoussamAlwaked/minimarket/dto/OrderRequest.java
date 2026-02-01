package com.HoussamAlwaked.minimarket.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    private String storeId;
    private List<OrderItemRequest> items = new ArrayList<>();
    private java.math.BigDecimal deliveryFee;

    public OrderRequest() {
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public java.math.BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(java.math.BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
}
