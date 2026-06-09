package com.krithe.store.controller;

import com.krithe.store.dto.ApiResponse;
import com.krithe.store.dto.OrderRequest;
import com.krithe.store.service.OrderService;
import com.krithe.store.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserContext userContext;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createOrder(@Valid @RequestBody OrderRequest req, HttpServletRequest request) {
        Long userId = userContext.getUserId(request);
        log.info("Received create order request for user {}: shippingName={}", userId, req.getShippingName());
        return ResponseEntity.ok(ApiResponse.ok("Order created", orderService.createOrder(userId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getOrders(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrders(userContext.getUserId(request))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getOrder(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(userContext.getUserId(request), id)));
    }
}
