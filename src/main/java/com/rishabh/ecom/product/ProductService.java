package com.rishabh.ecom.product;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "name", "price", "stockQty", "sku", "id"
    );

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Page<Product> search(String q, int page, int size, String sortBy, String order) {
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

        return repo.findAll(spec, pageable);
    }
}
