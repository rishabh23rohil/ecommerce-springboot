package com.rishabh.ecom.controller;

import com.rishabh.ecom.product.Product;
import com.rishabh.ecom.product.ProductController;
import com.rishabh.ecom.product.ProductService;
import com.rishabh.ecom.product.dto.ProductDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("CTRL-001");
        testProduct.setName("Controller Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQty(10);
        testProduct.setCreatedAt(Instant.now());
        testProduct.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should search products")
    void shouldSearchProducts() {
        // Given
        Page<Product> page = new PageImpl<>(List.of(testProduct), PageRequest.of(0, 20), 1);
        when(productService.search(any(), eq(0), eq(20), any(), any())).thenReturn(page);

        // When
        Page<Product> result = productController.search(null, 0, 20, "createdAt", "desc", null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productService, times(1)).search(any(), eq(0), eq(20), any(), any());
    }

    @Test
    @DisplayName("Should get product by ID")
    void shouldGetProductById() {
        // Given
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ResponseEntity<Product> response = productController.getProduct(1L, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getHeaders().getETag()).isNotNull();
    }

    @Test
    @DisplayName("Should return 304 Not Modified with valid ETag")
    void shouldReturn304WithValidETag() {
        // Given
        String etag = "\"" + testProduct.getUpdatedAt().toEpochMilli() + "\"";
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ResponseEntity<Product> response = productController.getProduct(1L, etag);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("Should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() {
        // Given
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Product> response = productController.getProduct(999L, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should create product")
    void shouldCreateProduct() {
        // Given
        ProductDtos.Create dto = new ProductDtos.Create(
            "New Product",
            "New Description",
            new BigDecimal("149.99"),
            "NEW-001",
            5
        );
        Product created = new Product();
        created.setId(2L);
        created.setSku(dto.sku());
        created.setName(dto.name());
        when(productService.createProduct(dto)).thenReturn(created);

        // When
        ResponseEntity<Product> response = productController.createProduct(dto);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSku()).isEqualTo("NEW-001");
        verify(productService, times(1)).createProduct(dto);
    }

    @Test
    @DisplayName("Should update product")
    void shouldUpdateProduct() {
        // Given
        ProductDtos.Update dto = new ProductDtos.Update(
            "Updated Product",
            "Updated Description",
            new BigDecimal("199.99"),
            15
        );
        when(productService.updateProduct(1L, dto)).thenReturn(Optional.of(testProduct));

        // When
        ResponseEntity<Product> response = productController.updateProduct(1L, dto);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(productService, times(1)).updateProduct(1L, dto);
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        // Given
        when(productService.deleteProduct(1L)).thenReturn(true);

        // When
        ResponseEntity<Void> response = productController.deleteProduct(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productService, times(1)).deleteProduct(1L);
    }
}

