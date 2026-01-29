package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.dto.StockDecrementRequest;
import com.HoussamAlwaked.minimarket.dto.StockUpdateRequest;
import com.HoussamAlwaked.minimarket.entity.Category;
import com.HoussamAlwaked.minimarket.entity.Product;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.CategoryRepository;
import com.HoussamAlwaked.minimarket.repository.ProductRepository;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
import com.HoussamAlwaked.minimarket.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores/{storeId}/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AccessControlService accessControlService;

    public ProductController(ProductRepository productRepository,
                             ProductService productService,
                             CategoryRepository categoryRepository,
                             StoreRepository storeRepository,
                             AccessControlService accessControlService) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<Product> getProducts(@PathVariable String storeId,
                                     @RequestParam(required = false) String categoryId) {
        ensureStoreExists(storeId);
        if (categoryId != null && !categoryId.isBlank()) {
            return productRepository.findByStoreAndCategory(storeId, categoryId);
        }
        return productRepository.findByStoreId(storeId);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@PathVariable String storeId,
                                                 @RequestBody Product request,
                                                 HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        ensureStoreExists(storeId);
        validateProduct(request, storeId);
        request.setId(null);
        request.setStoreId(storeId);
        Product saved = productRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String storeId,
                                 @PathVariable String id,
                                 @RequestBody Product request,
                                 HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        ensureStoreExists(storeId);
        validateProduct(request, storeId);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new BadRequestException("Product does not belong to store: " + storeId);
        }

        existing.setName(request.getName());
        existing.setCategoryId(request.getCategoryId());
        existing.setImage(request.getImage());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setStoreId(storeId);

        return productRepository.save(existing);
    }

    @PostMapping("/{id}/stock")
    public Product setStock(@PathVariable String storeId,
                            @PathVariable String id,
                            @RequestBody StockUpdateRequest request,
                            HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        ensureStoreExists(storeId);
        return productService.setStock(storeId, id, request);
    }

    @PostMapping("/{id}/stock/decrement")
    public Product decrementStock(@PathVariable String storeId,
                                  @PathVariable String id,
                                  @RequestBody StockDecrementRequest request,
                                  HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        ensureStoreExists(storeId);
        return productService.decrementStock(storeId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String storeId,
                                              @PathVariable String id,
                                              HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        ensureStoreExists(storeId);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new BadRequestException("Product does not belong to store: " + storeId);
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateProduct(Product product, String storeId) {
        if (product == null) {
            throw new BadRequestException("Product payload is required.");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Product name is required.");
        }
        if (product.getCategoryId() == null || product.getCategoryId().isBlank()) {
            throw new BadRequestException("Product category is required.");
        }
        Category category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Invalid category id: " + product.getCategoryId()));
        if (category.getStoreId() == null || !category.getStoreId().equals(storeId)) {
            throw new BadRequestException("Category does not belong to store: " + storeId);
        }
        if (product.getImage() != null && product.getImage().isBlank()) {
            throw new BadRequestException("Product image must not be blank.");
        }
        if (product.getPrice() == null) {
            throw new BadRequestException("Product price is required.");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Product price must be zero or greater.");
        }
        if (product.getStock() < 0) {
            throw new BadRequestException("Product stock must be zero or greater.");
        }
    }

    private void ensureStoreExists(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            throw new BadRequestException("Store id is required.");
        }
        if (!storeRepository.existsById(storeId)) {
            throw new NotFoundException("Store not found: " + storeId);
        }
    }
}