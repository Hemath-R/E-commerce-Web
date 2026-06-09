package com.krithe.store.service;

import com.krithe.store.dto.CartRequest;
import com.krithe.store.entity.Cart;
import com.krithe.store.entity.Product;
import com.krithe.store.exception.BadRequestException;
import com.krithe.store.exception.ResourceNotFoundException;
import com.krithe.store.repository.CartRepository;
import com.krithe.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public List<Map<String, Object>> getCart(Long userId) {
        return cartRepository.findByUserId(userId).stream().map(this::toCartItem).toList();
    }

    public Map<String, Object> addToCart(Long userId, CartRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        String size = req.getSize() != null ? req.getSize() : "M";
        Cart cart = cartRepository.findByUserIdAndProductIdAndSize(userId, req.getProductId(), size)
                .orElse(Cart.builder().userId(userId).productId(req.getProductId()).size(size).quantity(0).build());
        int newQty = cart.getQuantity() + req.getQuantity();
        if (newQty > product.getStock()) {
            throw new BadRequestException("Insufficient stock");
        }
        cart.setQuantity(newQty);
        cart = cartRepository.save(cart);
        return toCartItem(cart);
    }

    public Map<String, Object> updateCart(Long userId, Long cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!cart.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }
        Product product = productRepository.findById(cart.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (quantity > product.getStock()) {
            throw new BadRequestException("Insufficient stock");
        }
        cart.setQuantity(quantity);
        cart = cartRepository.save(cart);
        return toCartItem(cart);
    }

    public void removeFromCart(Long userId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!cart.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }
        cartRepository.delete(cart);
    }

    private Map<String, Object> toCartItem(Cart cart) {
        Product product = productRepository.findById(cart.getProductId()).orElse(null);
        Map<String, Object> item = new HashMap<>();
        item.put("id", cart.getId());
        item.put("productId", cart.getProductId());
        item.put("quantity", cart.getQuantity());
        item.put("size", cart.getSize());
        if (product != null) {
            item.put("name", product.getName());
            item.put("brand", product.getBrand());
            item.put("imageUrl", product.getImageUrl());
            item.put("price", product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice());
            item.put("originalPrice", product.getPrice());
            item.put("stock", product.getStock());
        }
        return item;
    }
}
