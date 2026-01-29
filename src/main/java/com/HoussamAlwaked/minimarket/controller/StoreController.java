package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.dto.AssignSubAdminRequest;
import com.HoussamAlwaked.minimarket.dto.StoreRequest;
import com.HoussamAlwaked.minimarket.entity.Store;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
import com.HoussamAlwaked.minimarket.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreRepository storeRepository;
    private final StoreService storeService;
    private final AccessControlService accessControlService;

    public StoreController(StoreRepository storeRepository,
                           StoreService storeService,
                           AccessControlService accessControlService) {
        this.storeRepository = storeRepository;
        this.storeService = storeService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<Store> getStores() {
        return storeRepository.findAll();
    }

    @GetMapping("/{storeId}")
    public Store getStore(@PathVariable String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found: " + storeId));
    }

    @PostMapping
    public ResponseEntity<Store> createStore(@RequestBody StoreRequest request,
                                             HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        Store saved = storeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{storeId}")
    public Store updateStore(@PathVariable String storeId,
                             @RequestBody StoreRequest request,
                             HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return storeService.update(storeId, request);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable String storeId,
                                            HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        if (!storeRepository.existsById(storeId)) {
            throw new NotFoundException("Store not found: " + storeId);
        }
        storeRepository.deleteById(storeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{storeId}/sub-admins")
    public Store assignSubAdmin(@PathVariable String storeId,
                                @RequestBody AssignSubAdminRequest request,
                                HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
            throw new BadRequestException("User id is required.");
        }
        return storeService.assignSubAdmin(storeId, request.getUserId());
    }

    @GetMapping("/{storeId}/sub-admins")
    public List<User> getSubAdmins(@PathVariable String storeId,
                                   HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return storeService.getSubAdmins(storeId);
    }
}