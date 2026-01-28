package com.HoussamAlwaked.minimarket.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.HoussamAlwaked.minimarket.entity.Category;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

    private final CollectionReference collection;

    public CategoryRepository(Firestore firestore) {
        this.collection = firestore.collection("categories");
    }

    public Category save(Category category) {
        if (category == null) {
            throw new BadRequestException("Category payload is required.");
        }
        if (category.getId() == null || category.getId().isBlank()) {
            category.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> data = toMap(category);
        try {
            collection.document(category.getId()).set(data).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Category save interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to save category.", ex.getCause());
        }
        return category;
    }

    public Optional<Category> findById(String id) {
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
            throw new RuntimeException("Category lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load category.", ex.getCause());
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
            throw new RuntimeException("Category lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load category.", ex.getCause());
        }
    }

    public List<Category> findAll() {
        try {
            QuerySnapshot snapshot = collection.get().get();
            List<Category> categories = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                categories.add(fromSnapshot(document));
            }
            return categories;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Category list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load categories.", ex.getCause());
        }
    }

    public Map<String, Object> toMap(Category category) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", category.getId());
        data.put("name", category.getName());
        data.put("slug", category.getSlug());
        return data;
    }

    public Category fromSnapshot(DocumentSnapshot snapshot) {
        Category category = new Category();
        category.setId(snapshot.getId());
        category.setName(snapshot.getString("name"));
        category.setSlug(snapshot.getString("slug"));
        return category;
    }
}
