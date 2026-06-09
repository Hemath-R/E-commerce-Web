package com.krithe.store.controller;

import com.krithe.store.dto.ApiResponse;
import com.krithe.store.security.JwtUtil;
import com.krithe.store.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProducts(search, category, sort, page, size)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<?>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getFeatured()));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<?>> getTrending() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getTrending()));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<?>> getRecentlyViewed(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(productService.getRecentlyViewed(userId)));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<?>> getProduct(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id, userId)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                return jwtUtil.getUserId(header.substring(7));
            } catch (Exception ignored) {}
        }
        return null;
    }
}
