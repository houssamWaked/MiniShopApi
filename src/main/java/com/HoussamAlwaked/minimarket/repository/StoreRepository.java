package com.HoussamAlwaked.minimarket.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.HoussamAlwaked.minimarket.entity.Store;
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
public class StoreRepository {

    private final CollectionReference collection;

    public StoreRepository(Firestore firestore) {
        this.collection = firestore.collection("stores");
    }

    public Store save(Store store) {
        if (store == null) {
            throw new BadRequestException("Store payload is required.");
        }
        if (store.getId() == null || store.getId().isBlank()) {
            store.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> data = toMap(store);
        try {
            collection.document(store.getId()).set(data).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Store save interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to save store.", ex.getCause());
        }
        return store;
    }

    public Optional<Store> findById(String id) {
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
            throw new RuntimeException("Store lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load store.", ex.getCause());
        }
    }

    public List<Store> findAll() {
        try {
            QuerySnapshot snapshot = collection.get().get();
            List<Store> stores = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                stores.add(fromSnapshot(document));
            }
            return stores;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Store list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load stores.", ex.getCause());
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
            throw new RuntimeException("Store lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load store.", ex.getCause());
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
            throw new RuntimeException("Store delete interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to delete store.", ex.getCause());
        }
    }

    public DocumentReference getDocument(String id) {
        return collection.document(id);
    }

    public Map<String, Object> toMap(Store store) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", store.getId());
        data.put("name", store.getName());
        data.put("address", store.getAddress());
        data.put("subAdminIds", store.getSubAdminIds());
        return data;
    }

    public Store fromSnapshot(DocumentSnapshot snapshot) {
        Store store = new Store();
        store.setId(snapshot.getId());
        store.setName(snapshot.getString("name"));
        store.setAddress(snapshot.getString("address"));
        Object raw = snapshot.get("subAdminIds");
        if (raw instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> subAdmins = (List<String>) raw;
            store.setSubAdminIds(subAdmins);
        }
        return store;
    }
}
