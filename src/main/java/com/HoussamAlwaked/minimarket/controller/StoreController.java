package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.dto.AssignSubAdminRequest;
import com.HoussamAlwaked.minimarket.dto.StoreRequest;
import com.HoussamAlwaked.minimarket.dto.StoreResponse;
import com.HoussamAlwaked.minimarket.entity.Store;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
import com.HoussamAlwaked.minimarket.service.OfferService;
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
    private final OfferService offerService;

    public StoreController(StoreRepository storeRepository,
                           StoreService storeService,
                           AccessControlService accessControlService,
                           OfferService offerService) {
        this.storeRepository = storeRepository;
        this.storeService = storeService;
        this.accessControlService = accessControlService;
        this.offerService = offerService;
    }

    @GetMapping
    public List<StoreResponse> getStores() {
        List<Store> stores = storeRepository.findAll();
        List<StoreResponse> responses = new java.util.ArrayList<>();
        for (Store store : stores) {
            responses.add(toResponse(store));
        }
        return responses;
    }

    @GetMapping("/{storeId}")
    public StoreResponse getStore(@PathVariable String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found: " + storeId));
        return toResponse(store);
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request,
                                                     HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        Store saved = storeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{storeId}")
    public StoreResponse updateStore(@PathVariable String storeId,
                                     @RequestBody StoreRequest request,
                                     HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return toResponse(storeService.update(storeId, request));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable String storeId,
                                            HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        storeService.deleteStore(storeId);
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

    @DeleteMapping("/{storeId}/sub-admins/{userId}")
    public Store removeSubAdmin(@PathVariable String storeId,
                                @PathVariable String userId,
                                HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return storeService.removeSubAdmin(storeId, userId);
    }

    @GetMapping("/{storeId}/sub-admins")
    public List<User> getSubAdmins(@PathVariable String storeId,
                                   HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return storeService.getSubAdmins(storeId);
    }

    private StoreResponse toResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setName(store.getName());
        response.setAddress(store.getAddress());
        response.setCategoryId(store.getCategoryId());
        response.setSubAdminIds(store.getSubAdminIds());
        response.setHasActiveOffer(offerService.hasActiveOffer(store.getId()));
        return response;
    }
}
