package com.krithe.store.repository;

import com.krithe.store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFeaturedTrue();
    List<Product> findByTrendingTrue();

    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:category IS NULL OR p.category = :category)")
    Page<Product> searchProducts(@Param("search") String search, @Param("category") String category, Pageable pageable);

    List<Product> findByCategoryAndIdNot(String category, Long id);
}
