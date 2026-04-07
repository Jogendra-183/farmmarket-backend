package com.farmmarket.config;

import com.farmmarket.entity.*;
import com.farmmarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded. Skipping initialization.");
            return;
        }

        logger.info("Seeding database with sample data...");

        // Create Admin
        User admin = User.builder()
                .name("Admin User")
                .email("admin@farmmarket.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(User.Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(admin);

        // Create Farmers
        User farmer1 = User.builder()
                .name("Jogendra")
                .email("jogendrasai780@gmail.com")
                .password(passwordEncoder.encode("Jogendrasai2007@"))
                .role(User.Role.FARMER)
                .phoneNumber("+1-555-123-4567")
                .address("123 Farm Road")
                .city("Springfield")
                .state("IL")
                .farmName("Green Valley Farm")
                .farmLocation("Springfield, IL")
                .farmBio("Family-owned organic farm since 1985. We grow fresh vegetables and fruits.")
                .isActive(true)
                .build();
        userRepository.save(farmer1);

        User farmer2 = User.builder()
                .name("Sarah Meadows")
                .email("sarah@farmmarket.com")
                .password(passwordEncoder.encode("Farmer@123"))
                .role(User.Role.FARMER)
                .phoneNumber("+1-555-987-6543")
                .farmName("Sunny Meadows Dairy")
                .farmLocation("Riverside, CA")
                .farmBio("Providing fresh dairy and eggs from happy, free-range animals.")
                .isActive(true)
                .build();
        userRepository.save(farmer2);

        // Create Buyer
        User buyer = User.builder()
                .name("Alice Johnson")
                .email("alice@farmmarket.com")
                .password(passwordEncoder.encode("Buyer@123"))
                .role(User.Role.BUYER)
                .phoneNumber("+1-555-555-0100")
                .address("456 Main St")
                .city("Chicago")
                .state("IL")
                .zipCode("60601")
                .isActive(true)
                .build();
        userRepository.save(buyer);

        // Create buyer subscription
        Subscription subscription = Subscription.builder()
                .user(buyer)
                .planType(Subscription.PlanType.PREMIUM)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .monthlyPrice(new BigDecimal("19.99"))
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();
        subscriptionRepository.save(subscription);

        // Create Products for farmer1
        Product[] products = {
                Product.builder()
                        .name("Organic Tomatoes")
                        .description(
                                "Sun-ripened organic tomatoes grown without pesticides. Rich in flavor and nutrients.")
                        .price(new BigDecimal("3.99"))
                        .stock(100)
                        .unit("kg")
                        .category(Product.Category.VEGETABLES)
                        .imageUrl("https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400")
                        .isAvailable(true)
                        .rating(new BigDecimal("4.8"))
                        .reviewCount(24)
                        .farmer(farmer1)
                        .build(),
                Product.builder()
                        .name("Fresh Strawberries")
                        .description("Hand-picked strawberries at peak ripeness. Perfect for desserts and smoothies.")
                        .price(new BigDecimal("5.49"))
                        .stock(50)
                        .unit("kg")
                        .category(Product.Category.FRUITS)
                        .imageUrl("https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400")
                        .isAvailable(true)
                        .rating(new BigDecimal("4.9"))
                        .reviewCount(31)
                        .farmer(farmer1)
                        .build(),
                Product.builder()
                        .name("Sweet Corn")
                        .description("Farm-fresh sweet corn picked daily. Great for grilling or boiling.")
                        .price(new BigDecimal("2.99"))
                        .stock(200)
                        .unit("dozen")
                        .category(Product.Category.VEGETABLES)
                        .imageUrl("https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=400")
                        .isAvailable(true)
                        .rating(new BigDecimal("4.6"))
                        .reviewCount(18)
                        .farmer(farmer1)
                        .build(),
                Product.builder()
                        .name("Organic Honey")
                        .description("Raw, unfiltered honey from our local beehives. Pure and natural sweetness.")
                        .price(new BigDecimal("12.99"))
                        .stock(30)
                        .unit("jar")
                        .category(Product.Category.PANTRY)
                        .imageUrl("https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=400")
                        .isAvailable(true)
                        .rating(new BigDecimal("5.0"))
                        .reviewCount(42)
                        .farmer(farmer2)
                        .build(),
                Product.builder()
                        .name("Farm Fresh Eggs")
                        .description("Free-range eggs from happy hens. Rich yolk and superior taste.")
                        .price(new BigDecimal("6.99"))
                        .stock(4) // Low stock for demo
                        .unit("dozen")
                        .category(Product.Category.DAIRY_AND_EGGS)
                        .imageUrl("https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400")
                        .isAvailable(true)
                        .rating(new BigDecimal("4.7"))
                        .reviewCount(56)
                        .farmer(farmer2)
                        .build()
        };

        for (Product p : products) {
            productRepository.save(p);
        }

        logger.info("✅ Database seeded successfully!");
        logger.info("📧 Admin: admin@farmmarket.com / Admin@123");
        logger.info("🌾 Farmer: jogendrasai780@gmail.com / Jogendrasai2007@");
        logger.info("🌾 Farmer: sarah@farmmarket.com / Farmer@123");
        logger.info("🛒 Buyer: alice@farmmarket.com / Buyer@123");
    }
}
