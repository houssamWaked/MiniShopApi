package com.HoussamAlwaked.minimarket.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
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
public class UserRepository {

    private final CollectionReference collection;

    public UserRepository(Firestore firestore) {
        this.collection = firestore.collection("users");
    }

    public User save(User user) {
        if (user == null) {
            throw new BadRequestException("User payload is required.");
        }
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> data = toMap(user);
        try {
            collection.document(user.getId()).set(data).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("User save interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to save user.", ex.getCause());
        }
        return user;
    }

    public Optional<User> findById(String id) {
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
            throw new RuntimeException("User lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load user.", ex.getCause());
        }
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            QuerySnapshot snapshot = collection.whereEqualTo("email", email).get().get();
            if (snapshot.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(fromSnapshot(snapshot.getDocuments().get(0)));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("User lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load user.", ex.getCause());
        }
    }

    public List<User> findAll() {
        try {
            QuerySnapshot snapshot = collection.get().get();
            List<User> users = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                users.add(fromSnapshot(document));
            }
            return users;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("User list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load users.", ex.getCause());
        }
    }

    public List<User> findByRole(UserRole role) {
        if (role == null) {
            return List.of();
        }
        try {
            QuerySnapshot snapshot = collection.whereEqualTo("role", role.name()).get().get();
            List<User> users = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                users.add(fromSnapshot(document));
            }
            return users;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("User list interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load users.", ex.getCause());
        }
    }

    public DocumentReference getDocument(String id) {
        return collection.document(id);
    }

    public Map<String, Object> toMap(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole() == null ? null : user.getRole().name());
        data.put("assignedStoreId", user.getAssignedStoreId());
        return data;
    }

    public User fromSnapshot(DocumentSnapshot snapshot) {
        User user = new User();
        user.setId(snapshot.getId());
        user.setName(snapshot.getString("name"));
        user.setEmail(snapshot.getString("email"));
        user.setAssignedStoreId(snapshot.getString("assignedStoreId"));
        String role = snapshot.getString("role");
        if (role != null) {
            user.setRole(UserRole.valueOf(role));
        }
        return user;
    }
}
