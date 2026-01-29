package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.dto.UserRequest;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public UserService(UserRepository userRepository, StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    public User create(UserRequest request, UserRole defaultRole) {
        validate(request);
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        UserRole role = request.getRole() == null ? defaultRole : request.getRole();
        user.setRole(role == null ? UserRole.CUSTOMER : role);

        if (user.getRole() == UserRole.SUB_ADMIN) {
            String storeId = request.getAssignedStoreId();
            if (storeId == null || storeId.isBlank()) {
                throw new BadRequestException("Assigned store id is required for sub-admin.");
            }
            if (!storeRepository.existsById(storeId)) {
                throw new NotFoundException("Store not found: " + storeId);
            }
            user.setAssignedStoreId(storeId);
        }

        return userRepository.save(user);
    }

    public User update(String id, UserRequest request) {
        validate(request);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());

        if (request.getRole() != null) {
            existing.setRole(request.getRole());
        }

        if (existing.getRole() == UserRole.SUB_ADMIN) {
            String storeId = request.getAssignedStoreId();
            if (storeId == null || storeId.isBlank()) {
                throw new BadRequestException("Assigned store id is required for sub-admin.");
            }
            if (!storeRepository.existsById(storeId)) {
                throw new NotFoundException("Store not found: " + storeId);
            }
            existing.setAssignedStoreId(storeId);
        } else {
            existing.setAssignedStoreId(null);
        }

        return userRepository.save(existing);
    }

    private void validate(UserRequest request) {
        if (request == null) {
            throw new BadRequestException("User payload is required.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("User name is required.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("User email is required.");
        }
    }
}
