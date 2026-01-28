package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.entity.Product;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.ProductRepository;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product request) {
        validateProduct(request);
        request.setId(null);
        Product saved = productRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product request) {
        validateProduct(request);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());

        return productRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new BadRequestException("Product payload is required.");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Product name is required.");
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
}
