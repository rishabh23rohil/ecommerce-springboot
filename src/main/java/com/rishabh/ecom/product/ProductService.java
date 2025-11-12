package com.rishabh.ecom.product;

import com.rishabh.ecom.product.dto.ProductDtos;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "name", "price", "stockQty", "sku", "id"
    );

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    @Timed(value = "product.search", description = "Time taken to search products")
    @Cacheable(value = "products", key = "(#q != null ? #q : 'null') + '-' + #page + '-' + #size + '-' + (#sortBy != null ? #sortBy : 'null') + '-' + (#order != null ? #order : 'null')")
    public Page<Product> search(String q, int page, int size, String sortBy, String order) {
        log.info("🔴 CACHE MISS: products search - q={}, page={}, size={}, sortBy={}, order={}", q, page, size, sortBy, order);
        
        // sanitize paging
        page = Math.max(page, 0);
        size = (size <= 0 || size > 200) ? 20 : size;

        // sanitize sort field
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        // sanitize direction
        Sort.Direction dir = Sort.Direction.DESC;
        if (order != null) {
            dir = Sort.Direction.fromOptionalString(order).orElse(Sort.Direction.DESC);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        // build spec (q filters by sku or name)
        Specification<Product> spec = ProductSpecifications.matchingQuery(q);

        Page<Product> result = repo.findAll(spec, pageable);
        log.info("💾 CACHE POPULATED: products search - {} results cached", result.getTotalElements());
        return result;
    }

    @Timed(value = "product.getById", description = "Time taken to get product by ID")
    @Cacheable(value = "productById", key = "#id")
    public Optional<Product> getProductById(Long id) {
        log.info("🔴 CACHE MISS: productById - id={}", id);
        Optional<Product> product = repo.findById(id);
        if (product.isPresent()) {
            log.info("💾 CACHE POPULATED: productById - id={} cached", id);
        }
        return product;
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    @Transactional
    public Product createProduct(ProductDtos.Create dto) {
        log.info("Cache evicted: creating product - sku={}", dto.sku());
        Product product = Product.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .sku(dto.sku())
                .stockQty(dto.stockQty())
                .build();
        return repo.save(product);
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    @Transactional
    public Optional<Product> updateProduct(Long id, ProductDtos.Update dto) {
        log.info("Cache evicted: updating product - id={}", id);
        return repo.findById(id)
                .map(existing -> {
                    if (dto.name() != null) existing.setName(dto.name());
                    if (dto.description() != null) existing.setDescription(dto.description());
                    if (dto.price() != null) existing.setPrice(dto.price());
                    if (dto.stockQty() != null) existing.setStockQty(dto.stockQty());
                    return repo.save(existing);
                });
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    @Transactional
    public boolean deleteProduct(Long id) {
        log.info("Cache evicted: deleting product - id={}", id);
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
