package com.rishabh.ecom.product;

import com.rishabh.ecom.product.dto.ProductDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Products", description = "Product management endpoints with caching and ETag support")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping(path = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Search products", description = "Returns paginated products with caching. Supports search query, pagination, and sorting.")
    @GetMapping
    public Page<Product> search(
            @Parameter(description = "Search query (filters by SKU or name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order (asc/desc)")
            @RequestParam(defaultValue = "desc") String order,
            org.springframework.security.core.Authentication authentication
    ) {
        // Authentication parameter helps Spring Security recognize the authenticated user
        return service.search(q, page, size, sortBy, order);
    }

    @Operation(
        summary = "Get product by ID",
        description = "Returns a single product with ETag support for conditional requests. " +
                     "Use If-None-Match header with the ETag value to get 304 Not Modified if unchanged."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Parameter(description = "ETag from previous response for conditional GET")
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    ) {
        Optional<Product> productOpt = service.getProductById(id);
        
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        String currentETag = "\"" + product.getUpdatedAt().toEpochMilli() + "\"";

        // Handle conditional GET with If-None-Match
        if (ifNoneMatch != null && ifNoneMatch.equals(currentETag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(currentETag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(currentETag)
                .body(product);
    }

    @Operation(summary = "Create product", description = "Creates a new product. Requires ADMIN role. Invalidates product cache.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductDtos.Create dto
    ) {
        Product product = service.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "Update product", description = "Updates an existing product. Requires ADMIN role. Invalidates product cache.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Valid @RequestBody ProductDtos.Update dto
    ) {
        Optional<Product> updated = service.updateProduct(id, dto);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete product", description = "Deletes a product. Requires ADMIN role. Invalidates product cache.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id
    ) {
        boolean deleted = service.deleteProduct(id);
        return deleted 
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
