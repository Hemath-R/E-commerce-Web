package com.krithe.store.controller;

import com.krithe.store.dto.ApiResponse;
import com.krithe.store.dto.CartRequest;
import com.krithe.store.service.CartService;
import com.krithe.store.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserContext userContext;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCart(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(userContext.getUserId(request))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addToCart(@Valid @RequestBody CartRequest req, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Added to cart", cartService.addToCart(userContext.getUserId(request), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateCart(@PathVariable Long id, @RequestBody Map<String, Integer> body,
                                                       HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.updateCart(userContext.getUserId(request), id, body.get("quantity"))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable Long id, HttpServletRequest request) {
        cartService.removeFromCart(userContext.getUserId(request), id);
        return ResponseEntity.ok(ApiResponse.ok("Removed from cart", null));
    }
}
