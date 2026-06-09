package com.krithe.store.service;

import com.krithe.store.dto.OrderRequest;
import com.krithe.store.entity.*;
import com.krithe.store.enums.OrderStatus;
import com.krithe.store.exception.BadRequestException;
import com.krithe.store.exception.ResourceNotFoundException;
import com.krithe.store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public Order createOrder(Long userId, OrderRequest req) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        log.info("Creating order for user {} with {} items", userId, cartItems.size());

        // Validate and compute total first
        BigDecimal total = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            Product product = productRepository.findById(cart.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            if (cart.getQuantity() > product.getStock()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }
            BigDecimal price = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        // Create and persist order without items to generate ID
        Order order = Order.builder()
                .userId(userId)
                .orderNumber("KS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(OrderStatus.PENDING)
                .shippingName(req.getShippingName())
                .shippingEmail(req.getShippingEmail())
                .shippingPhone(req.getShippingPhone())
                .shippingAddress(req.getShippingAddress())
                .shippingCity(req.getShippingCity())
                .shippingState(req.getShippingState())
                .shippingPincode(req.getShippingPincode())
                .build();

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        log.debug("Order created id={} number={}", saved.getId(), saved.getOrderNumber());

        // Create OrderItems with order reference, add and save
        for (Cart cart : cartItems) {
            Product product = productRepository.findById(cart.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            BigDecimal price = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(cart.getQuantity())
                    .price(price)
                    .size(cart.getSize())
                    .build();
            item.setOrder(saved);
            saved.getItems().add(item);
        }

        Order finalOrder = orderRepository.save(saved);
        log.info("Order {} persisted with {} items", finalOrder.getOrderNumber(), finalOrder.getItems().size());
        return finalOrder;
    }

    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }
        return order;
    }

    @Transactional
    public Order finalizeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        cartRepository.deleteByUserId(order.getUserId());
        return order;
    }
}
