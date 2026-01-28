package com.yourname.urlshortener.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.yourname.urlshortener.entity.Product;
import com.yourname.urlshortener.exception.BadRequestException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private final CollectionReference collection;

    public ProductRepository(Firestore firestore) {
        this.collection = firestore.collection("products");
    }

    public Product save(Product product) {
        if (product == null) {
            throw new BadRequestException("Product payload is required.");
        }
        if (product.getId() == null || product.getId().isBlank()) {
            product.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> data = toMap(product);
        try {
            collection.document(product.getId()).set(data).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Product save interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to save product.", ex.getCause());
        }
        return product;
    }

    public Optional<Product> findById(String id) {
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
            throw new RuntimeException("Product lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load product.", ex.getCause());
        }
    }

    public List<Product> findAll() {
        try {
            QuerySnapshot snapshot = collection.get().get();
            List<Product> products = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                products.add(fromSnapshot(document));
            }
            return products;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Product list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load products.", ex.getCause());
        }
    }

    public boolean existsById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        try {
            DocumentSnapshot snapshot = collection.document(id).get().get();
            return snapshot.exists();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Product lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load product.", ex.getCause());
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
            throw new RuntimeException("Product delete interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to delete product.", ex.getCause());
        }
    }

    public DocumentReference getDocument(String id) {
        return collection.document(id);
    }

    public Map<String, Object> toMap(Product product) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", product.getId());
        data.put("name", product.getName());
        data.put("image", product.getImage());
        data.put("price", product.getPrice() == null ? null : product.getPrice().toPlainString());
        data.put("stock", product.getStock());
        return data;
    }

    public Product fromSnapshot(DocumentSnapshot snapshot) {
        Product product = new Product();
        product.setId(snapshot.getId());
        product.setName(snapshot.getString("name"));
        product.setImage(snapshot.getString("image"));
        product.setPrice(parseDecimal(snapshot.get("price")));
        Long stockValue = snapshot.getLong("stock");
        product.setStock(stockValue == null ? 0 : stockValue.intValue());
        return product;
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
