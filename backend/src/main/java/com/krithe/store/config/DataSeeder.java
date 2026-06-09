package com.krithe.store.config;

import com.krithe.store.entity.Product;
import com.krithe.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        productRepository.saveAll(java.util.List.of(
            Product.builder().name("Air Monarch Elite").description("Premium leather sneakers with cushioned sole and breathable mesh upper.")
                .brand("Nike").category("Sneakers").price(new BigDecimal("12999")).discountPrice(new BigDecimal("10999"))
                .stock(50).imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600")
                .rating(new BigDecimal("4.8")).reviewCount(234).featured(true).trending(true).build(),
            Product.builder().name("Velocity Runner Pro").description("Lightweight performance running shoes with responsive foam.")
                .brand("Adidas").category("Running").price(new BigDecimal("14999")).discountPrice(new BigDecimal("12499"))
                .stock(35).imageUrl("https://images.unsplash.com/photo-1606107557192-0a74c4c4d6ac?w=600")
                .rating(new BigDecimal("4.9")).reviewCount(189).featured(true).trending(true).build(),
            Product.builder().name("Classic Oxford Heritage").description("Handcrafted Italian leather oxford shoes for formal occasions.")
                .brand("Gucci").category("Formal").price(new BigDecimal("24999")).discountPrice(new BigDecimal("21999"))
                .stock(20).imageUrl("https://images.unsplash.com/photo-1614252239476-9523f5b9d0e2?w=600")
                .rating(new BigDecimal("4.7")).reviewCount(156).featured(true).trending(false).build(),
            Product.builder().name("Urban Street Flex").description("Street-style high tops with premium suede design.")
                .brand("Jordan").category("Sneakers").price(new BigDecimal("16999")).discountPrice(new BigDecimal("14999"))
                .stock(40).imageUrl("https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600")
                .rating(new BigDecimal("4.6")).reviewCount(312).featured(false).trending(true).build(),
            Product.builder().name("Trail Blazer X").description("All-terrain hiking boots with waterproof membrane.")
                .brand("Salomon").category("Outdoor").price(new BigDecimal("11999")).discountPrice(new BigDecimal("9999"))
                .stock(25).imageUrl("https://images.unsplash.com/photo-1525966222134-fcfa99b8d077?w=600")
                .rating(new BigDecimal("4.5")).reviewCount(98).featured(false).trending(true).build(),
            Product.builder().name("Slip-On Luxe").description("Minimalist slip-on loafers in supple calfskin leather.")
                .brand("Tod's").category("Casual").price(new BigDecimal("18999")).discountPrice(new BigDecimal("16499"))
                .stock(30).imageUrl("https://images.unsplash.com/photo-1533867610401-7e5b1e0e0b0f?w=600")
                .rating(new BigDecimal("4.8")).reviewCount(87).featured(true).trending(false).build(),
            Product.builder().name("Court Master 88").description("Retro basketball-inspired sneakers with gum sole.")
                .brand("New Balance").category("Sneakers").price(new BigDecimal("9999")).discountPrice(new BigDecimal("8499"))
                .stock(60).imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600")
                .rating(new BigDecimal("4.4")).reviewCount(445).featured(false).trending(true).build(),
            Product.builder().name("Chelsea Boot Noir").description("Sleek Chelsea boots in polished leather.")
                .brand("Dr. Martens").category("Boots").price(new BigDecimal("13999")).discountPrice(new BigDecimal("11999"))
                .stock(28).imageUrl("https://images.unsplash.com/photo-1638247025967-f4e8f9640a60?w=600")
                .rating(new BigDecimal("4.7")).reviewCount(134).featured(true).trending(true).build()
        ));
    }
}
