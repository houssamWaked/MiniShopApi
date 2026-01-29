package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.dto.StoreRequest;
import com.HoussamAlwaked.minimarket.entity.Store;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public StoreService(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    public Store create(StoreRequest request) {
        validate(request);
        Store store = new Store();
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        return storeRepository.save(store);
    }

    public Store update(String id, StoreRequest request) {
        validate(request);
        Store existing = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found: " + id));
        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
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
