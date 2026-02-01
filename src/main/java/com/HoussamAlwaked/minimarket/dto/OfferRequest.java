package com.HoussamAlwaked.minimarket.dto;

import com.HoussamAlwaked.minimarket.entity.OfferScope;
import com.HoussamAlwaked.minimarket.entity.OfferType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OfferRequest {

    private String name;
    private boolean enabled = true;
    private OfferScope scope;
    private OfferType discountType;
    private BigDecimal discountValue;
    private boolean freeDelivery;
    private BigDecimal minOrderTotal;
    private String categoryId;
    private List<String> productIds = new ArrayList<>();
    private Instant validFrom;
    private Instant validTo;
    private String startTime;
    private String endTime;
    private List<Integer> daysOfWeek = new ArrayList<>();

    public OfferRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OfferScope getScope() {
        return scope;
    }

    public void setScope(OfferScope scope) {
        this.scope = scope;
    }

    public OfferType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(OfferType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public boolean isFreeDelivery() {
        return freeDelivery;
    }

    public void setFreeDelivery(boolean freeDelivery) {
        this.freeDelivery = freeDelivery;
    }

    public BigDecimal getMinOrderTotal() {
        return minOrderTotal;
    }

    public void setMinOrderTotal(BigDecimal minOrderTotal) {
        this.minOrderTotal = minOrderTotal;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds == null ? new ArrayList<>() : productIds;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek == null ? new ArrayList<>() : daysOfWeek;
    }
}
