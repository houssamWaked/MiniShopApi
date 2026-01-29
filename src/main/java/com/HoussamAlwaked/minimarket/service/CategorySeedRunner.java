package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.entity.Category;
import com.HoussamAlwaked.minimarket.repository.CategoryRepository;
import com.HoussamAlwaked.minimarket.repository.StoreRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategorySeedRunner implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public CategorySeedRunner(CategoryRepository categoryRepository, StoreRepository storeRepository) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public void run(String... args) {
        String defaultStoreId = System.getenv("DEFAULT_STORE_ID");
        if (defaultStoreId == null || defaultStoreId.isBlank()) {
            return;
        }

        if (!storeRepository.existsById(defaultStoreId)) {
            return;
        }

        List<Category> existing = categoryRepository.findByStoreId(defaultStoreId);
        if (existing != null && !existing.isEmpty()) {
            return;
        }

        seedCategory(defaultStoreId, "fresh-produce", "Fresh Produce");
        seedCategory(defaultStoreId, "dairy", "Dairy");
        seedCategory(defaultStoreId, "bakery", "Bakery");
        seedCategory(defaultStoreId, "beverages", "Beverages");
        seedCategory(defaultStoreId, "snacks", "Snacks");
        seedCategory(defaultStoreId, "household", "Household");
    }

    private void seedCategory(String storeId, String slug, String name) {
        Category category = new Category();
        category.setStoreId(storeId);
        category.setSlug(slug);
        category.setName(name);
        categoryRepository.save(category);
    }
}
