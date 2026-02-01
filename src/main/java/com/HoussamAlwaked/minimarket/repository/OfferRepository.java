package com.HoussamAlwaked.minimarket.repository;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.HoussamAlwaked.minimarket.entity.Offer;
import com.HoussamAlwaked.minimarket.entity.OfferScope;
import com.HoussamAlwaked.minimarket.entity.OfferType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class OfferRepository {

    private final CollectionReference collection;

    public OfferRepository(Firestore firestore) {
        this.collection = firestore.collection("offers");
    }

    public Offer save(Offer offer) {
        if (offer.getId() == null || offer.getId().isBlank()) {
            offer.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> data = toMap(offer);
        try {
            collection.document(offer.getId()).set(data).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Offer save interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to save offer.", ex.getCause());
        }
        return offer;
    }

    public Optional<Offer> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot snapshot = collection.document(id).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }
            return Optional.of(fromSnapshot(snapshot));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Offer lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load offer.", ex.getCause());
        }
    }

    public List<Offer> findByStoreId(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            return List.of();
        }
        try {
            QuerySnapshot snapshot = collection.whereEqualTo("storeId", storeId).get().get();
            List<Offer> offers = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                offers.add(fromSnapshot(document));
            }
            return offers;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Offer list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load offers.", ex.getCause());
        }
    }

    public void deleteById(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        try {
            collection.document(id).delete().get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Offer delete interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to delete offer.", ex.getCause());
        }
    }

    public DocumentReference getDocument(String id) {
        return collection.document(id);
    }

    public Map<String, Object> toMap(Offer offer) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", offer.getId());
        data.put("storeId", offer.getStoreId());
        data.put("name", offer.getName());
        data.put("enabled", offer.isEnabled());
        data.put("scope", offer.getScope() == null ? null : offer.getScope().name());
        data.put("discountType", offer.getDiscountType() == null ? null : offer.getDiscountType().name());
        data.put("discountValue", offer.getDiscountValue() == null ? null : offer.getDiscountValue().toPlainString());
        data.put("freeDelivery", offer.isFreeDelivery());
        data.put("minOrderTotal", offer.getMinOrderTotal() == null ? null : offer.getMinOrderTotal().toPlainString());
        data.put("categoryId", offer.getCategoryId());
        data.put("productIds", offer.getProductIds());
        data.put("validFrom", offer.getValidFrom() == null ? null : offer.getValidFrom().toEpochMilli());
        data.put("validTo", offer.getValidTo() == null ? null : offer.getValidTo().toEpochMilli());
        data.put("startTime", offer.getStartTime());
        data.put("endTime", offer.getEndTime());
        data.put("daysOfWeek", offer.getDaysOfWeek());
        data.put("createdAt", offer.getCreatedAt() == null ? null : offer.getCreatedAt().toEpochMilli());
        data.put("updatedAt", offer.getUpdatedAt() == null ? null : offer.getUpdatedAt().toEpochMilli());
        return data;
    }

    public Offer fromSnapshot(DocumentSnapshot snapshot) {
        Offer offer = new Offer();
        offer.setId(snapshot.getId());
        offer.setStoreId(snapshot.getString("storeId"));
        offer.setName(snapshot.getString("name"));
        offer.setEnabled(Boolean.TRUE.equals(snapshot.getBoolean("enabled")));
        String scope = snapshot.getString("scope");
        if (scope != null) {
            offer.setScope(OfferScope.valueOf(scope));
        }
        String discountType = snapshot.getString("discountType");
        if (discountType != null) {
            offer.setDiscountType(OfferType.valueOf(discountType));
        }
        offer.setDiscountValue(parseDecimal(snapshot.get("discountValue")));
        offer.setFreeDelivery(Boolean.TRUE.equals(snapshot.getBoolean("freeDelivery")));
        offer.setMinOrderTotal(parseDecimal(snapshot.get("minOrderTotal")));
        offer.setCategoryId(snapshot.getString("categoryId"));
        Object productIds = snapshot.get("productIds");
        if (productIds instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) productIds;
            offer.setProductIds(list);
        }
        offer.setValidFrom(parseInstant(snapshot.get("validFrom")));
        offer.setValidTo(parseInstant(snapshot.get("validTo")));
        offer.setStartTime(snapshot.getString("startTime"));
        offer.setEndTime(snapshot.getString("endTime"));
        Object days = snapshot.get("daysOfWeek");
        if (days instanceof List) {
            List<Integer> parsed = new ArrayList<>();
            for (Object value : (List<?>) days) {
                if (value instanceof Number) {
                    parsed.add(((Number) value).intValue());
                } else if (value != null) {
                    parsed.add(Integer.parseInt(value.toString()));
                }
            }
            offer.setDaysOfWeek(parsed);
        }
        offer.setCreatedAt(parseInstant(snapshot.get("createdAt")));
        offer.setUpdatedAt(parseInstant(snapshot.get("updatedAt")));
        return offer;
    }

    private Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().toInstant();
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        return Instant.ofEpochMilli(Long.parseLong(value.toString()));
    }

    private BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }
}
