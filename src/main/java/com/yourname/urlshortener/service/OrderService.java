package com.yourname.urlshortener.service;

import com.yourname.urlshortener.dto.OrderItemRequest;
import com.yourname.urlshortener.dto.OrderRequest;
import com.yourname.urlshortener.entity.Order;
import com.yourname.urlshortener.entity.OrderItem;
import com.yourname.urlshortener.entity.Product;
import com.yourname.urlshortener.exception.BadRequestException;
import com.yourname.urlshortener.exception.NotFoundException;
import com.yourname.urlshortener.repository.OrderRepository;
import com.yourname.urlshortener.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order items are required.");
        }

        Order order = new Order();
        order.setCreatedAt(Instant.now());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest == null) {
                throw new BadRequestException("Order item is required.");
            }
            Long productId = itemRequest.getProductId();
            if (productId == null) {
                throw new BadRequestException("Product id is required.");
            }
            int quantity = itemRequest.getQuantity();
            if (quantity <= 0) {
                throw new BadRequestException("Quantity must be greater than zero.");
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

            if (product.getPrice() == null) {
                throw new BadRequestException("Product price is missing for id: " + productId);
            }

            if (product.getStock() < quantity) {
                throw new BadRequestException("Insufficient stock for product id: " + productId);
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(lineTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotal(total);

        return orderRepository.save(order);
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }
}
