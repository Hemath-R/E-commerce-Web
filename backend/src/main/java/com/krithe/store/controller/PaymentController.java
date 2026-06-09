package com.krithe.store.controller;

import com.krithe.store.dto.ApiResponse;
import com.krithe.store.dto.PaymentVerifyRequest;
import com.krithe.store.service.PaymentService;
import com.krithe.store.util.UserContext;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserContext userContext;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<?>> createRazorpayOrder(@RequestBody Map<String, Long> body,
                                                                HttpServletRequest request) throws RazorpayException {
        userContext.getUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(paymentService.createRazorpayOrder(body.get("orderId"))));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyPayment(@Valid @RequestBody PaymentVerifyRequest req,
                                                         HttpServletRequest request) {
        userContext.getUserId(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment verified", paymentService.verifyAndSavePayment(req)));
    }
}
