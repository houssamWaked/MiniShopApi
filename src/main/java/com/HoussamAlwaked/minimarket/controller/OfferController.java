package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.dto.OfferRequest;
import com.HoussamAlwaked.minimarket.entity.Offer;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
import com.HoussamAlwaked.minimarket.service.OfferService;
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
@RequestMapping("/api/stores/{storeId}/offers")
public class OfferController {

    private final OfferService offerService;
    private final AccessControlService accessControlService;

    public OfferController(OfferService offerService, AccessControlService accessControlService) {
        this.offerService = offerService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<Offer> listOffers(@PathVariable String storeId, HttpServletRequest request) {
        accessControlService.requireStoreManager(request, storeId);
        return offerService.listByStore(storeId);
    }

    @PostMapping
    public ResponseEntity<Offer> createOffer(@PathVariable String storeId,
                                             @RequestBody OfferRequest request,
                                             HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        Offer saved = offerService.create(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{offerId}")
    public Offer updateOffer(@PathVariable String storeId,
                             @PathVariable String offerId,
                             @RequestBody OfferRequest request,
                             HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        return offerService.update(storeId, offerId, request);
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteOffer(@PathVariable String storeId,
                                            @PathVariable String offerId,
                                            HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        offerService.delete(storeId, offerId);
        return ResponseEntity.noContent().build();
    }
}
