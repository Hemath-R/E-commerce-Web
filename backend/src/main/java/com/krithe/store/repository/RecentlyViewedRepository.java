package com.krithe.store.repository;

import com.krithe.store.entity.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {
    List<RecentlyViewed> findTop10ByUserIdOrderByViewedAtDesc(Long userId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
