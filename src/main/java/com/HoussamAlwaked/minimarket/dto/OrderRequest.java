package com.HoussamAlwaked.minimarket.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    private String storeId;
    private List<OrderItemRequest> items = new ArrayList<>();

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
}
