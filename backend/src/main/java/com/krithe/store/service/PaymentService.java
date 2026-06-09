package com.krithe.store.service;

import com.krithe.store.dto.PaymentVerifyRequest;
import com.krithe.store.entity.Order;
import com.krithe.store.entity.Payment;
import com.krithe.store.enums.PaymentStatus;
import com.krithe.store.exception.BadRequestException;
import com.krithe.store.exception.ResourceNotFoundException;
import com.krithe.store.repository.OrderRepository;
import com.krithe.store.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Map<String, Object> createRazorpayOrder(Long orderId) throws RazorpayException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject options = new JSONObject();
        options.put("amount", order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue());
        options.put("currency", "INR");
        options.put("receipt", order.getOrderNumber());

        com.razorpay.Order razorpayOrder = client.orders.create(options);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(Payment.builder()
                        .orderId(orderId)
                        .amount(order.getTotalAmount())
                        .status(PaymentStatus.CREATED)
                        .build());
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("razorpayOrderId", razorpayOrder.get("id"));
        result.put("amount", order.getTotalAmount());
        result.put("currency", "INR");
        result.put("keyId", razorpayKeyId);
        result.put("orderNumber", order.getOrderNumber());
        return result;
    }

    @Transactional
    public Map<String, Object> verifyAndSavePayment(PaymentVerifyRequest req) {
        if (!verifySignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
            throw new BadRequestException("Invalid payment signature");
        }

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Payment payment = paymentRepository.findByOrderId(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setMethod(req.getMethod());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        Order finalized = orderService.finalizeOrder(req.getOrderId());

        Map<String, Object> result = new HashMap<>();
        result.put("order", finalized);
        result.put("payment", payment);
        return result;
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
