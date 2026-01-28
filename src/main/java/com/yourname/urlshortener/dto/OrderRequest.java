package com.yourname.urlshortener.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    private List<OrderItemRequest> items = new ArrayList<>();

    public OrderRequest() {
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
