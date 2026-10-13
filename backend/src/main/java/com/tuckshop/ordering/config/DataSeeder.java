package com.tuckshop.ordering.config;

import com.tuckshop.ordering.entity.*;
import com.tuckshop.ordering.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(
            AdminUserRepository adminRepository,
            ShopSettingsRepository settingsRepository,
            ProductRepository productRepository,
            CouponRepository couponRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Seed Admin User
            if (adminRepository.findByUsername("admin").isEmpty()) {
                AdminUser admin = new AdminUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                adminRepository.save(admin);
                System.out.println("Default admin user created: username='admin', password='admin123'");
            }

            // 2. Seed Shop Settings
            if (settingsRepository.count() == 0) {
                ShopSettings settings = new ShopSettings();
                settings.setShopName("Campus Tuck Shop");
                settings.setIsOpen(true);
                settings.setOpeningTime("08:00");
                settings.setClosingTime("22:00");
                settings.setMinOrderAmount(BigDecimal.valueOf(10));
                settings.setMaxOrderAmount(BigDecimal.valueOf(5000));
                settings.setDailyOrderLimit(500);
                settings.setEstimatedPreparationMinutes(10);
                settings.setPickupLocation("Counter 1, Main Tuck Shop");
                settingsRepository.save(settings);
                System.out.println("Default Shop Settings initialized.");
            }

            // 3. Seed Products if empty
            if (productRepository.count() == 0) {
                List<Product> products = List.of(
                    // Snacks
                    createProduct("Classic Crispy Samosa", "Snacks", BigDecimal.valueOf(15), BigDecimal.valueOf(12),
                            60, 10, true, "Golden-fried crispy pastry stuffed with spiced potatoes and green peas.",
                            "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=500&q=80"),
                    createProduct("Masala Peri Peri Fries", "Snacks", BigDecimal.valueOf(60), BigDecimal.valueOf(50),
                            45, 5, true, "Hot and crispy golden fries tossed in zesty peri-peri spice blend.",
                            "https://images.unsplash.com/photo-1576107232684-1279f3908594?w=500&q=80"),
                    createProduct("Veg Grilled Cheese Sandwich", "Snacks", BigDecimal.valueOf(70), BigDecimal.valueOf(60),
                            30, 4, true, "Triple-layer toasted sandwich stuffed with seasoned veggies and melted cheese.",
                            "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=500&q=80"),
                    createProduct("Cheese Maggi Delight", "Snacks", BigDecimal.valueOf(45), BigDecimal.valueOf(40),
                            50, 4, true, "Classic noodles prepared with butter, secret herbs, and a layer of melted cheese.",
                            "https://images.unsplash.com/photo-1612927601601-6638404737ce?w=500&q=80"),
                    createProduct("Paneer Tikka Roll", "Snacks", BigDecimal.valueOf(80), BigDecimal.valueOf(70),
                            25, 4, true, "Soft paratha wrap rolled with smoky marinated paneer cubes and mint dip.",
                            "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=500&q=80"),

                    // Drinks
                    createProduct("Iced Cold Coffee", "Drinks", BigDecimal.valueOf(50), BigDecimal.valueOf(45),
                            50, 5, true, "Rich and creamy blended cold brew coffee topped with dark chocolate drizzle.",
                            "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=500&q=80"),
                    createProduct("Mango Frooti Chill", "Drinks", BigDecimal.valueOf(20), null,
                            80, 10, true, "Classic sweet mango drink pack, perfect thirst quencher on a hot day.",
                            "https://images.unsplash.com/photo-1546833998-877b37c2e5c6?w=500&q=80"),
                    createProduct("Thums Up 500ml", "Drinks", BigDecimal.valueOf(35), null,
                            60, 6, true, "Chilled fizzy cola with strong kick in convenient 500ml bottle.",
                            "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500&q=80"),
                    createProduct("Fresh Lime Mint Cooler", "Drinks", BigDecimal.valueOf(35), BigDecimal.valueOf(30),
                            40, 5, true, "Freshly squeezed zesty lemon juice infused with garden mint and rock salt.",
                            "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500&q=80"),

                    // Bakery & Desserts
                    createProduct("Warm Chocolate Brownie", "Bakery", BigDecimal.valueOf(60), BigDecimal.valueOf(50),
                            30, 4, true, "Gooey Belgian chocolate fudge brownie topped with walnut bits.",
                            "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=500&q=80"),
                    createProduct("Crispy Veg Puff", "Bakery", BigDecimal.valueOf(25), BigDecimal.valueOf(20),
                            45, 6, true, "Flaky multi-layered puff pastry filled with spiced mixed vegetable masala.",
                            "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&q=80"),
                    createProduct("Blueberry Streusel Muffin", "Bakery", BigDecimal.valueOf(45), BigDecimal.valueOf(40),
                            20, 3, true, "Soft golden bakery muffin bursting with fresh wild blueberries.",
                            "https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=500&q=80"),

                    // Chocolates
                    createProduct("Cadbury Dairy Milk Silk", "Chocolates", BigDecimal.valueOf(70), BigDecimal.valueOf(65),
                            40, 5, true, "Silky smooth melt-in-mouth milk chocolate bar.",
                            "https://images.unsplash.com/photo-1549007953-2f2dc0b24019?w=500&q=80"),
                    createProduct("Nestle KitKat 4-Finger", "Chocolates", BigDecimal.valueOf(30), BigDecimal.valueOf(25),
                            50, 6, true, "Crisp wafer fingers coated in smooth milk chocolate.",
                            "https://images.unsplash.com/photo-1582293041079-7814c2f12063?w=500&q=80"),

                    // Stationery
                    createProduct("Premium Spiral Notebook A5", "Stationery", BigDecimal.valueOf(80), BigDecimal.valueOf(70),
                            35, 5, true, "200-page ruled notebook with premium 80gsm paper and frosted poly cover.",
                            "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500&q=80"),
                    createProduct("Gel Pen Pack (5 Colors)", "Stationery", BigDecimal.valueOf(50), BigDecimal.valueOf(45),
                            50, 10, true, "Smooth smudge-proof 0.5mm Japanese ink gel pens in assorted colors.",
                            "https://images.unsplash.com/photo-1585336261026-41866838a982?w=500&q=80")
                );

                productRepository.saveAll(products);
                System.out.println("Seeded " + products.size() + " tuck shop products.");
            }

            // 4. Seed Coupons if empty
            if (couponRepository.count() == 0) {
                Coupon c1 = new Coupon();
                c1.setCode("WELCOME10");
                c1.setDiscountType(DiscountType.PERCENTAGE);
                c1.setDiscountValue(BigDecimal.valueOf(10));
                c1.setMinimumOrderAmount(BigDecimal.valueOf(40));
                c1.setMaximumDiscount(BigDecimal.valueOf(30));
                c1.setActive(true);

                Coupon c2 = new Coupon();
                c2.setCode("SNACK20");
                c2.setDiscountType(DiscountType.PERCENTAGE);
                c2.setDiscountValue(BigDecimal.valueOf(20));
                c2.setMinimumOrderAmount(BigDecimal.valueOf(100));
                c2.setMaximumDiscount(BigDecimal.valueOf(50));
                c2.setActive(true);

                Coupon c3 = new Coupon();
                c3.setCode("FLAT50");
                c3.setDiscountType(DiscountType.FIXED);
                c3.setDiscountValue(BigDecimal.valueOf(50));
                c3.setMinimumOrderAmount(BigDecimal.valueOf(200));
                c3.setActive(true);

                couponRepository.saveAll(List.of(c1, c2, c3));
                System.out.println("Seeded default coupons: WELCOME10, SNACK20, FLAT50.");
            }
        };
    }

    private Product createProduct(String name, String category, BigDecimal price, BigDecimal discountedPrice,
                                  int stock, int maxQty, boolean available, String description, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setDiscountedPrice(discountedPrice);
        p.setStockQuantity(stock);
        p.setMaxQuantityPerOrder(maxQty);
        p.setAvailable(available);
        p.setDescription(description);
        p.setImageUrl(imageUrl);
        return p;
    }
}
