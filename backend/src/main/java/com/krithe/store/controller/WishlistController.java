package com.krithe.store.controller;

import com.krithe.store.dto.ApiResponse;
import com.krithe.store.service.WishlistService;
import com.krithe.store.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;
    private final UserContext userContext;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getWishlist(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getWishlist(userContext.getUserId(request))));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> add(@PathVariable Long productId, HttpServletRequest request) {
        wishlistService.addToWishlist(userContext.getUserId(request), productId);
        return ResponseEntity.ok(ApiResponse.ok("Added to wishlist", null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId, HttpServletRequest request) {
        wishlistService.removeFromWishlist(userContext.getUserId(request), productId);
        return ResponseEntity.ok(ApiResponse.ok("Removed from wishlist", null));
    }
}
