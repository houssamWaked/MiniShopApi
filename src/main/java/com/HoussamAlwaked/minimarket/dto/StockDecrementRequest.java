package com.HoussamAlwaked.minimarket.dto;

public class StockDecrementRequest {

    private int quantity;

    public StockDecrementRequest() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
