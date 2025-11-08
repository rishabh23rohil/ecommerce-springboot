package com.rishabh.ecom.product;

import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> matchingQuery(String q) {
        if (q == null || q.isBlank()) {
            return Specification.where(null);
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("sku")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }
}
