package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.entity.Category;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.CategoryRepository;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
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
@RequestMapping("/api/stores/{storeId}/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AccessControlService accessControlService;

    public CategoryController(CategoryRepository categoryRepository,
                              StoreRepository storeRepository,
                              AccessControlService accessControlService) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<Category> getCategories(@PathVariable String storeId) {
        ensureStoreExists(storeId);
        return categoryRepository.findByStoreId(storeId);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@PathVariable String storeId,
                                                   @RequestBody Category request,
                                                   HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        validateCategory(request);
        ensureStoreExists(storeId);
        request.setId(null);
        request.setStoreId(storeId);
        Category saved = categoryRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable String storeId,
                                   @PathVariable String id,
                                   @RequestBody Category request,
                                   HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        validateCategory(request);
        ensureStoreExists(storeId);
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new BadRequestException("Category does not belong to store: " + storeId);
        }

        existing.setName(request.getName());
        existing.setSlug(request.getSlug());
        existing.setStoreId(storeId);

        return categoryRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String storeId,
                                               @PathVariable String id,
                                               HttpServletRequest servletRequest) {
        accessControlService.requireStoreManager(servletRequest, storeId);
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new BadRequestException("Category does not belong to store: " + storeId);
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new BadRequestException("Category payload is required.");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new BadRequestException("Category name is required.");
        }
        if (category.getSlug() != null && category.getSlug().isBlank()) {
            throw new BadRequestException("Category slug must not be blank.");
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