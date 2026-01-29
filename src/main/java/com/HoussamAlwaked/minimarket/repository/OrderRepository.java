package com.HoussamAlwaked.minimarket.repository;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.HoussamAlwaked.minimarket.entity.Order;
import com.HoussamAlwaked.minimarket.entity.OrderItem;
import com.HoussamAlwaked.minimarket.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private final CollectionReference collection;

    public OrderRepository(Firestore firestore) {
        this.collection = firestore.collection("orders");
    }

    public Order save(Order order) {
        if (order.getId() == null || order.getId().isBlank()) {
            order.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> data = toMap(order);
        try {
            collection.document(order.getId()).set(data).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Order save interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to save order.", ex.getCause());
        }
        return order;
    }

    public List<Order> findAll() {
        try {
            QuerySnapshot snapshot = collection.get().get();
            List<Order> orders = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                orders.add(fromSnapshot(document));
            }
            return orders;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Order list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load orders.", ex.getCause());
        }
    }

    public List<Order> findByStoreId(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            return List.of();
        }
        try {
            QuerySnapshot snapshot = collection.whereEqualTo("storeId", storeId).get().get();
            List<Order> orders = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                orders.add(fromSnapshot(document));
            }
            return orders;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Order list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load orders.", ex.getCause());
        }
    }

    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return List.of();
        }
        try {
            QuerySnapshot snapshot = collection.whereEqualTo("customerId", customerId).get().get();
            List<Order> orders = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                orders.add(fromSnapshot(document));
            }
            return orders;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Order list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load orders.", ex.getCause());
        }
    }

    public DocumentReference getDocument(String id) {
        return collection.document(id);
    }

    public Map<String, Object> toMap(Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("customerId", order.getCustomerId());
        data.put("storeId", order.getStoreId());
        data.put("createdAt", order.getCreatedAt() == null ? null : order.getCreatedAt().toEpochMilli());
        data.put("total", order.getTotal() == null ? null : order.getTotal().toPlainString());
        data.put("status", order.getStatus());

        List<Map<String, Object>> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                items.add(toMap(item));
            }
        }
        data.put("orderItems", items);
        return data;
    }

    public Order fromSnapshot(DocumentSnapshot snapshot) {
        Order order = new Order();
        order.setId(snapshot.getId());
        order.setCustomerId(snapshot.getString("customerId"));
        order.setStoreId(snapshot.getString("storeId"));
        order.setCreatedAt(parseInstant(snapshot.get("createdAt")));
        order.setTotal(parseDecimal(snapshot.get("total")));
        order.setStatus(snapshot.getString("status"));

        List<OrderItem> items = new ArrayList<>();
        Object itemsRaw = snapshot.get("orderItems");
        if (itemsRaw instanceof List) {
            List<?> rawList = (List<?>) itemsRaw;
            for (Object rawItem : rawList) {
                if (rawItem instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) rawItem;
                    items.add(fromMap(itemMap));
                }
            }
        }
        order.setOrderItems(items);
        return order;
    }

    private Map<String, Object> toMap(OrderItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", item.getId());
        data.put("quantity", item.getQuantity());
        data.put("price", item.getPrice() == null ? null : item.getPrice().toPlainString());
        data.put("product", toMap(item.getProduct()));
        return data;
    }

    private Map<String, Object> toMap(Product product) {
        if (product == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", product.getId());
        data.put("name", product.getName());
        data.put("categoryId", product.getCategoryId());
        data.put("storeId", product.getStoreId());
        data.put("image", product.getImage());
        data.put("price", product.getPrice() == null ? null : product.getPrice().toPlainString());
        data.put("stock", product.getStock());
        return data;
    }

    private OrderItem fromMap(Map<String, Object> map) {
        OrderItem item = new OrderItem();
        item.setId(asString(map.get("id")));
        item.setQuantity(parseInt(map.get("quantity")));
        item.setPrice(parseDecimal(map.get("price")));
        Object productRaw = map.get("product");
        if (productRaw instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> productMap = (Map<String, Object>) productRaw;
            item.setProduct(fromProductMap(productMap));
        }
        return item;
    }

    private Product fromProductMap(Map<String, Object> map) {
        Product product = new Product();
        product.setId(asString(map.get("id")));
        product.setName(asString(map.get("name")));
        product.setCategoryId(asString(map.get("categoryId")));
        product.setStoreId(asString(map.get("storeId")));
        product.setImage(asString(map.get("image")));
        product.setPrice(parseDecimal(map.get("price")));
        product.setStock(parseInt(map.get("stock")));
        return product;
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

    private int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
