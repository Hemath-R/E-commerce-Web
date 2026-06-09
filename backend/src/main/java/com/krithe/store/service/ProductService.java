package com.krithe.store.service;

import com.krithe.store.entity.Product;
import com.krithe.store.entity.RecentlyViewed;
import com.krithe.store.exception.ResourceNotFoundException;
import com.krithe.store.repository.ProductRepository;
import com.krithe.store.repository.RecentlyViewedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RecentlyViewedRepository recentlyViewedRepository;

    public Page<Product> getProducts(String search, String category, String sort, int page, int size) {
        Sort sorting = switch (sort != null ? sort : "default") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("rating").descending();
            default -> Sort.by("createdAt").descending();
        };
        return productRepository.searchProducts(
                search != null && !search.isBlank() ? search : null,
                category != null && !category.isBlank() ? category : null,
                PageRequest.of(page, size, sorting));
    }

    @Transactional
    public Map<String, Object> getProductById(Long id, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (userId != null) {
            recentlyViewedRepository.deleteByUserIdAndProductId(userId, id);
            recentlyViewedRepository.save(RecentlyViewed.builder().userId(userId).productId(id).build());
        }
        List<Product> related = productRepository.findByCategoryAndIdNot(product.getCategory(), id)
                .stream().limit(4).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("relatedProducts", related);
        return result;
    }

    public List<Product> getFeatured() {
        return productRepository.findByFeaturedTrue();
    }

    public List<Product> getTrending() {
        return productRepository.findByTrendingTrue();
    }

    public List<Product> getRecentlyViewed(Long userId) {
        return recentlyViewedRepository.findTop10ByUserIdOrderByViewedAtDesc(userId)
                .stream()
                .map(rv -> productRepository.findById(rv.getProductId()).orElse(null))
                .filter(p -> p != null)
                .toList();
    }
}
