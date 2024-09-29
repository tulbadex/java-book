package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import demo.repository.CategoryRepository;
import demo.repository.RoleRepository;
import io.jsonwebtoken.security.Keys;
import demo.model.Category;
import demo.model.Role;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

@Component
public class DataSeeder {
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;

    private String secretKey;

    public DataSeeder(CategoryRepository categoryRepository, RoleRepository roleRepository) {
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
    }

    @Bean
    public CommandLineRunner seedCategories() {
        return args -> {
            seedCategoryIfNotExists("Fiction");
            seedCategoryIfNotExists("Non-fiction");

            seedRolesIfNotExists();
            generateSecretKey();
        };
    }

    private void seedCategoryIfNotExists(String categoryName) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);

        if (existingCategory.isEmpty()) {
            Category category = new Category();
            category.setName(categoryName);
            categoryRepository.save(category);
            System.out.println("Category '" + categoryName + "' seeded.");
        } else {
            System.out.println("Category '" + categoryName + "' already exists. Skipping seed.");
        }
    }

    private void seedRolesIfNotExists() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_BOOK_AUTHOR", "ROLE_BOOK_WRITER");

        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("Role '" + roleName + "' seeded.");
            } else {
                System.out.println("Role '" + roleName + "' already exists. Skipping seed.");
            }
        }
    }

    public void generateSecretKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
            secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
            System.out.println("Generated Base64-encoded key: " + secretKey);
            // Ideally store this key securely (e.g., in an environment variable or a config server)
        }
    }
}