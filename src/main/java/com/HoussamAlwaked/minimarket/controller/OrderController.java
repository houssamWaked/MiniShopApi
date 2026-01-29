package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.dto.OrderRequest;
import com.HoussamAlwaked.minimarket.entity.Order;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
import com.HoussamAlwaked.minimarket.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AccessControlService accessControlService;

    public OrderController(OrderService orderService, AccessControlService accessControlService) {
        this.orderService = orderService;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request,
                                             HttpServletRequest servletRequest) {
        User user = accessControlService.requireUser(servletRequest);
        Order created = orderService.createOrder(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Order> getOrders(@RequestParam(required = false) String storeId,
                                 HttpServletRequest servletRequest) {
        User user = accessControlService.requireUser(servletRequest);
        return orderService.getOrders(user, storeId);
    }
}