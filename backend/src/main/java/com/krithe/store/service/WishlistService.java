package com.krithe.store.service;

import com.krithe.store.entity.Product;
import com.krithe.store.entity.Wishlist;
import com.krithe.store.exception.BadRequestException;
import com.krithe.store.repository.ProductRepository;
import com.krithe.store.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public List<Product> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(w -> productRepository.findById(w.getProductId()).orElse(null))
                .filter(p -> p != null)
                .toList();
    }

    public void addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new BadRequestException("Already in wishlist");
        }
        wishlistRepository.save(Wishlist.builder().userId(userId).productId(productId).build());
    }

    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(wishlistRepository::delete);
    }
}
