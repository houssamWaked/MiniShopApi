package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.dto.OfferRequest;
import com.HoussamAlwaked.minimarket.entity.Offer;
import com.HoussamAlwaked.minimarket.entity.OfferScope;
import com.HoussamAlwaked.minimarket.entity.OfferType;
import com.HoussamAlwaked.minimarket.entity.OrderItem;
import com.HoussamAlwaked.minimarket.entity.Product;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.OfferRepository;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class OfferService {

    public static class OfferApplication {
        private Offer offer;
        private BigDecimal discount = BigDecimal.ZERO;
        private boolean freeDelivery;

        public Offer getOffer() {
            return offer;
        }

        public void setOffer(Offer offer) {
            this.offer = offer;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public void setDiscount(BigDecimal discount) {
            this.discount = discount;
        }

        public boolean isFreeDelivery() {
            return freeDelivery;
        }

        public void setFreeDelivery(boolean freeDelivery) {
            this.freeDelivery = freeDelivery;
        }
    }

    private final OfferRepository offerRepository;
    private final StoreRepository storeRepository;

    public OfferService(OfferRepository offerRepository, StoreRepository storeRepository) {
        this.offerRepository = offerRepository;
        this.storeRepository = storeRepository;
    }

    public Offer create(String storeId, OfferRequest request) {
        validate(storeId, request);
        Offer offer = new Offer();
        apply(offer, storeId, request);
        offer.setCreatedAt(Instant.now());
        offer.setUpdatedAt(Instant.now());
        return offerRepository.save(offer);
    }

    public Offer update(String storeId, String id, OfferRequest request) {
        validate(storeId, request);
        Offer existing = offerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new BadRequestException("Offer does not belong to store: " + storeId);
        }
        apply(existing, storeId, request);
        existing.setUpdatedAt(Instant.now());
        return offerRepository.save(existing);
    }

    public void delete(String storeId, String id) {
        Offer existing = offerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new BadRequestException("Offer does not belong to store: " + storeId);
        }
        offerRepository.deleteById(id);
    }

    public List<Offer> listByStore(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            return List.of();
        }
        return offerRepository.findByStoreId(storeId);
    }

    public boolean hasActiveOffer(String storeId) {
        return !getActiveOffers(storeId, Instant.now()).isEmpty();
    }

    public List<Offer> getActiveOffers(String storeId, Instant now) {
        List<Offer> offers = offerRepository.findByStoreId(storeId);
        List<Offer> active = new ArrayList<>();
        for (Offer offer : offers) {
            if (isActive(offer, now)) {
                active.add(offer);
            }
        }
        return active;
    }

    public OfferApplication applyOffers(String storeId,
                                        List<OrderItem> orderItems,
                                        BigDecimal subtotal,
                                        Instant now) {
        OfferApplication result = new OfferApplication();
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return result;
        }
        List<Offer> offers = getActiveOffers(storeId, now);
        BigDecimal maxDiscount = BigDecimal.ZERO;
        Offer bestOffer = null;
        boolean freeDelivery = false;

        for (Offer offer : offers) {
            if (!meetsMinTotal(offer, subtotal)) {
                continue;
            }
            BigDecimal base = applicableSubtotal(offer, orderItems);
            if (base.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal discount = computeDiscount(offer, base);
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                bestOffer = offer;
            }
            if (offer.isFreeDelivery()) {
                freeDelivery = true;
                if (bestOffer == null && maxDiscount.compareTo(BigDecimal.ZERO) == 0) {
                    bestOffer = offer;
                }
            }
        }

        result.setOffer(bestOffer);
        result.setDiscount(maxDiscount);
        result.setFreeDelivery(freeDelivery);
        return result;
    }

    private void validate(String storeId, OfferRequest request) {
        if (storeId == null || storeId.isBlank()) {
            throw new BadRequestException("Store id is required.");
        }
        if (!storeRepository.existsById(storeId)) {
            throw new NotFoundException("Store not found: " + storeId);
        }
        if (request == null) {
            throw new BadRequestException("Offer payload is required.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Offer name is required.");
        }
        if (request.getScope() == null) {
            throw new BadRequestException("Offer scope is required.");
        }
        if (request.getDiscountType() == null && !request.isFreeDelivery()) {
            throw new BadRequestException("Offer discount type is required.");
        }
        if (request.getDiscountType() != null
                && (request.getDiscountValue() == null
                || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BadRequestException("Offer discount value is required.");
        }
        if (request.getScope() == OfferScope.CATEGORY
                && (request.getCategoryId() == null || request.getCategoryId().isBlank())) {
            throw new BadRequestException("Category id is required for category offer.");
        }
        if ((request.getScope() == OfferScope.PRODUCT || request.getScope() == OfferScope.PRODUCTS)
                && (request.getProductIds() == null || request.getProductIds().isEmpty())) {
            throw new BadRequestException("Product ids are required for product offer.");
        }
    }

    private void apply(Offer offer, String storeId, OfferRequest request) {
        offer.setStoreId(storeId);
        offer.setName(request.getName());
        offer.setEnabled(request.isEnabled());
        offer.setScope(request.getScope());
        offer.setDiscountType(request.getDiscountType());
        offer.setDiscountValue(request.getDiscountValue());
        offer.setFreeDelivery(request.isFreeDelivery());
        offer.setMinOrderTotal(request.getMinOrderTotal());
        offer.setCategoryId(request.getCategoryId());
        offer.setProductIds(request.getProductIds());
        offer.setValidFrom(request.getValidFrom());
        offer.setValidTo(request.getValidTo());
        offer.setStartTime(request.getStartTime());
        offer.setEndTime(request.getEndTime());
        offer.setDaysOfWeek(request.getDaysOfWeek());
    }

    private boolean isActive(Offer offer, Instant now) {
        if (offer == null || !offer.isEnabled()) {
            return false;
        }
        if (offer.getValidFrom() != null && now.isBefore(offer.getValidFrom())) {
            return false;
        }
        if (offer.getValidTo() != null && now.isAfter(offer.getValidTo())) {
            return false;
        }
        if (offer.getDaysOfWeek() != null && !offer.getDaysOfWeek().isEmpty()) {
            ZonedDateTime zoned = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
            int day = zoned.getDayOfWeek().getValue();
            if (!offer.getDaysOfWeek().contains(day)) {
                return false;
            }
        }
        if (offer.getStartTime() != null && offer.getEndTime() != null) {
            LocalTime start = parseTime(offer.getStartTime());
            LocalTime end = parseTime(offer.getEndTime());
            if (start != null && end != null) {
                LocalTime nowTime = LocalTime.now(ZoneId.systemDefault());
                if (end.isBefore(start)) {
                    // Overnight window
                    if (nowTime.isBefore(start) && nowTime.isAfter(end)) {
                        return false;
                    }
                } else if (nowTime.isBefore(start) || nowTime.isAfter(end)) {
                    return false;
                }
            }
        }
        return true;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean meetsMinTotal(Offer offer, BigDecimal subtotal) {
        if (offer.getMinOrderTotal() == null) {
            return true;
        }
        return subtotal.compareTo(offer.getMinOrderTotal()) >= 0;
    }

    private BigDecimal applicableSubtotal(Offer offer, List<OrderItem> orderItems) {
        if (offer.getScope() == OfferScope.ALL_PRODUCTS) {
            return sumItems(orderItems);
        }
        if (offer.getScope() == OfferScope.CATEGORY) {
            return sumItemsByCategory(orderItems, offer.getCategoryId());
        }
        if (offer.getScope() == OfferScope.PRODUCT || offer.getScope() == OfferScope.PRODUCTS) {
            return sumItemsByProducts(orderItems, offer.getProductIds());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal computeDiscount(Offer offer, BigDecimal base) {
        if (offer.getDiscountType() == null || offer.getDiscountValue() == null) {
            return BigDecimal.ZERO;
        }
        if (offer.getDiscountType() == OfferType.PERCENT) {
            return base.multiply(offer.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }
        return offer.getDiscountValue().min(base);
    }

    private BigDecimal sumItems(List<OrderItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        if (items == null) {
            return sum;
        }
        for (OrderItem item : items) {
            if (item.getPrice() == null) {
                continue;
            }
            sum = sum.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return sum;
    }

    private BigDecimal sumItemsByCategory(List<OrderItem> items, String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product == null || product.getCategoryId() == null) {
                continue;
            }
            if (categoryId.equals(product.getCategoryId()) && item.getPrice() != null) {
                sum = sum.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return sum;
    }

    private BigDecimal sumItemsByProducts(List<OrderItem> items, List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product == null || product.getId() == null) {
                continue;
            }
            if (productIds.contains(product.getId()) && item.getPrice() != null) {
                sum = sum.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return sum;
    }
}
