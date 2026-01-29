package com.HoussamAlwaked.minimarket.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.HoussamAlwaked.minimarket.dto.StockDecrementRequest;
import com.HoussamAlwaked.minimarket.dto.StockUpdateRequest;
import com.HoussamAlwaked.minimarket.entity.Product;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.ProductRepository;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final Firestore firestore;

    public ProductService(ProductRepository productRepository, Firestore firestore) {
        this.productRepository = productRepository;
        this.firestore = firestore;
    }

    public Product setStock(String storeId, String productId, StockUpdateRequest request) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product id is required.");
        }
        if (request == null) {
            throw new BadRequestException("Stock payload is required.");
        }
        if (request.getStock() < 0) {
            throw new BadRequestException("Stock must be zero or greater.");
        }

        try {
            ApiFuture<Product> future = firestore.runTransaction(transaction -> {
                DocumentReference productRef = productRepository.getDocument(productId);
                DocumentSnapshot snapshot = getSnapshot(transaction.get(productRef));
                if (!snapshot.exists()) {
                    throw new NotFoundException("Product not found: " + productId);
                }

                Product product = productRepository.fromSnapshot(snapshot);
                if (storeId != null && !storeId.equals(product.getStoreId())) {
                    throw new BadRequestException("Product does not belong to store: " + storeId);
                }
                product.setStock(request.getStock());
                transaction.set(productRef, productRepository.toMap(product));
                return product;
            });
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Stock update interrupted.", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Failed to update stock.", cause);
        }
    }

    public Product decrementStock(String storeId, String productId, StockDecrementRequest request) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product id is required.");
        }
        if (request == null) {
            throw new BadRequestException("Stock decrement payload is required.");
        }
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero.");
        }

        try {
            ApiFuture<Product> future = firestore.runTransaction(transaction -> {
                DocumentReference productRef = productRepository.getDocument(productId);
                DocumentSnapshot snapshot = getSnapshot(transaction.get(productRef));
                if (!snapshot.exists()) {
                    throw new NotFoundException("Product not found: " + productId);
                }

                Product product = productRepository.fromSnapshot(snapshot);
                if (storeId != null && !storeId.equals(product.getStoreId())) {
                    throw new BadRequestException("Product does not belong to store: " + storeId);
                }
                if (product.getStock() < request.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product id: " + productId);
                }

                product.setStock(product.getStock() - request.getQuantity());
                transaction.set(productRef, productRepository.toMap(product));
                return product;
            });
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Stock decrement interrupted.", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Failed to decrement stock.", cause);
        }
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
