package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.dto.StoreRequest;
import com.HoussamAlwaked.minimarket.entity.Store;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.CategoryRepository;
import com.HoussamAlwaked.minimarket.repository.ProductRepository;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public StoreService(StoreRepository storeRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        CategoryRepository categoryRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Store create(StoreRequest request) {
        validate(request);
        Store store = new Store();
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setCategoryId(request.getCategoryId());
        return storeRepository.save(store);
    }

    public Store update(String id, StoreRequest request) {
        validate(request);
        Store existing = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found: " + id));
        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setCategoryId(request.getCategoryId());
        return storeRepository.save(existing);
    }

    public Store assignSubAdmin(String storeId, String userId) {
        if (storeId == null || storeId.isBlank()) {
            throw new BadRequestException("Store id is required.");
        }
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User id is required.");
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found: " + storeId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        user.setRole(UserRole.SUB_ADMIN);
        user.setAssignedStoreId(storeId);
        userRepository.save(user);

        List<String> subAdmins = new ArrayList<>(store.getSubAdminIds());
        if (!subAdmins.contains(userId)) {
            subAdmins.add(userId);
        }
        store.setSubAdminIds(subAdmins);
        return storeRepository.save(store);
    }

    public Store removeSubAdmin(String storeId, String userId) {
        if (storeId == null || storeId.isBlank()) {
            throw new BadRequestException("Store id is required.");
        }
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User id is required.");
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found: " + storeId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (store.getSubAdminIds() != null && store.getSubAdminIds().remove(userId)) {
            storeRepository.save(store);
        }

        if (storeId.equals(user.getAssignedStoreId())) {
            user.setAssignedStoreId(null);
            if (user.getRole() == UserRole.SUB_ADMIN) {
                user.setRole(UserRole.CUSTOMER);
            }
            userRepository.save(user);
        }

        if (user.getEmail() != null) {
            userRepository.findAllByEmailIgnoreCase(user.getEmail()).forEach(match -> {
                if (!storeId.equals(match.getAssignedStoreId())) {
                    return;
                }
                match.setAssignedStoreId(null);
                if (match.getRole() == UserRole.SUB_ADMIN) {
                    match.setRole(UserRole.CUSTOMER);
                }
                userRepository.save(match);
            });
        }

        return store;
    }

    public void deleteStore(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            throw new BadRequestException("Store id is required.");
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found: " + storeId));

        // Delete all products for this store.
        productRepository.findByStoreId(storeId)
                .forEach(product -> productRepository.deleteById(product.getId()));

        // Delete all categories for this store.
        categoryRepository.findByStoreId(storeId)
                .forEach(category -> categoryRepository.deleteById(category.getId()));

        // Demote and unassign all sub-admins for this store.
        for (String userId : store.getSubAdminIds()) {
            userRepository.findById(userId).ifPresent(user -> {
                if (storeId.equals(user.getAssignedStoreId())) {
                    user.setAssignedStoreId(null);
                    if (user.getRole() == UserRole.SUB_ADMIN) {
                        user.setRole(UserRole.CUSTOMER);
                    }
                    userRepository.save(user);
                }
            });
        }

        storeRepository.deleteById(storeId);
    }

    public List<User> getSubAdmins(String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found: " + storeId));
        List<User> users = new ArrayList<>();
        for (String userId : store.getSubAdminIds()) {
            userRepository.findById(userId).ifPresent(users::add);
        }
        return users;
    }

    private void validate(StoreRequest request) {
        if (request == null) {
            throw new BadRequestException("Store payload is required.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Store name is required.");
        }
        if (request.getAddress() != null && request.getAddress().isBlank()) {
            throw new BadRequestException("Store address must not be blank.");
        }
    }
}
