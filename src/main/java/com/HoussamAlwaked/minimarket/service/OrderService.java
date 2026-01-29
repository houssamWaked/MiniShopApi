package com.HoussamAlwaked.minimarket.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.HoussamAlwaked.minimarket.dto.OrderItemRequest;
import com.HoussamAlwaked.minimarket.dto.OrderRequest;
import com.HoussamAlwaked.minimarket.entity.Order;
import com.HoussamAlwaked.minimarket.entity.OrderItem;
import com.HoussamAlwaked.minimarket.entity.Product;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
import com.HoussamAlwaked.minimarket.exception.ForbiddenException;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.OrderRepository;
import com.HoussamAlwaked.minimarket.repository.ProductRepository;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final Firestore firestore;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        StoreRepository storeRepository,
                        Firestore firestore) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.firestore = firestore;
    }

    public Order createOrder(String customerId, OrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order items are required.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new BadRequestException("Customer id is required.");
        }
        if (request.getStoreId() == null || request.getStoreId().isBlank()) {
            throw new BadRequestException("Store id is required.");
        }
        if (!storeRepository.existsById(request.getStoreId())) {
            throw new NotFoundException("Store not found: " + request.getStoreId());
        }

        try {
            ApiFuture<Order> future = firestore.runTransaction(transaction -> {
                Order order = new Order();
                order.setId(UUID.randomUUID().toString());
                order.setCustomerId(customerId);
                order.setStoreId(request.getStoreId());
                order.setCreatedAt(Instant.now());
                order.setStatus("PENDING");

                List<OrderItem> orderItems = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (OrderItemRequest itemRequest : request.getItems()) {
                    if (itemRequest == null) {
                        throw new BadRequestException("Order item is required.");
                    }
                    String productId = itemRequest.getProductId();
                    if (productId == null || productId.isBlank()) {
                        throw new BadRequestException("Product id is required.");
                    }
                    int quantity = itemRequest.getQuantity();
                    if (quantity <= 0) {
                        throw new BadRequestException("Quantity must be greater than zero.");
                    }

                    DocumentReference productRef = productRepository.getDocument(productId);
                    DocumentSnapshot snapshot = getSnapshot(transaction.get(productRef));
                    if (!snapshot.exists()) {
                        throw new NotFoundException("Product not found: " + productId);
                    }

                    Product product = productRepository.fromSnapshot(snapshot);
                    if (!request.getStoreId().equals(product.getStoreId())) {
                        throw new BadRequestException("Product does not belong to store: " + request.getStoreId());
                    }
                    if (product.getPrice() == null) {
                        throw new BadRequestException("Product price is missing for id: " + productId);
                    }
                    if (product.getStock() < quantity) {
                        throw new BadRequestException("Insufficient stock for product id: " + productId);
                    }

                    product.setStock(product.getStock() - quantity);
                    transaction.set(productRef, productRepository.toMap(product));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setId(UUID.randomUUID().toString());
                    orderItem.setProduct(product);
                    orderItem.setQuantity(quantity);
                    orderItem.setPrice(product.getPrice());
                    orderItems.add(orderItem);

                    BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                    total = total.add(lineTotal);
                }

                order.setOrderItems(orderItems);
                order.setTotal(total);

                DocumentReference orderRef = orderRepository.getDocument(order.getId());
                transaction.set(orderRef, orderRepository.toMap(order));
                return order;
            });

            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Order creation interrupted.", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Failed to create order.", cause);
        }
    }

    public List<Order> getOrders(User user, String storeId) {
        if (user == null) {
            return List.of();
        }
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Super admin cannot access orders.");
        }
        if (user.getRole() == UserRole.SUB_ADMIN) {
            return orderRepository.findByStoreId(user.getAssignedStoreId());
        }
        return orderRepository.findByCustomerId(user.getId());
    }

    private DocumentSnapshot getSnapshot(ApiFuture<DocumentSnapshot> future) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Document lookup interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to load document.", ex.getCause());
        }
    }
}
