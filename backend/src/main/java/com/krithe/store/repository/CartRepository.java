package com.krithe.store.repository;

import com.krithe.store.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserIdAndProductIdAndSize(Long userId, Long productId, String size);
    void deleteByUserId(Long userId);
}
