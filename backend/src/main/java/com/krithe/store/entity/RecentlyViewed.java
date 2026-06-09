package com.krithe.store.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recently_viewed")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RecentlyViewed {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @PrePersist
    void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}
