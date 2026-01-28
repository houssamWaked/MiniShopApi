package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.entity.Category;
import com.HoussamAlwaked.minimarket.repository.CategoryRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategorySeedRunner implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeedRunner(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        List<Category> existing = categoryRepository.findAll();
        if (existing != null && !existing.isEmpty()) {
            return;
        }

        seedCategory("fresh-produce", "Fresh Produce");
        seedCategory("dairy", "Dairy");
        seedCategory("bakery", "Bakery");
        seedCategory("beverages", "Beverages");
        seedCategory("snacks", "Snacks");
        seedCategory("household", "Household");
    }

    private void seedCategory(String slug, String name) {
        Category category = new Category();
        category.setSlug(slug);
        category.setName(name);
        categoryRepository.save(category);
    }
}
