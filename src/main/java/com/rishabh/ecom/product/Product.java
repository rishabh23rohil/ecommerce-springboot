package com.rishabh.ecom.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "products", indexes = {
    @Index(name = "idx_products_sku", columnList = "sku", unique = true),
    @Index(name = "idx_products_name", columnList = "name")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank @Size(max = 120)
  private String name;

  @Size(max = 1000)
  private String description;

  @NotNull @DecimalMin("0.00")
  @Column(precision = 14, scale = 2, nullable = false)
  private BigDecimal price;

  @NotBlank @Size(max = 40)
  @Column(nullable = false, unique = true)
  private String sku;

  @NotNull @Min(0)
  @Column(name = "stock_qty", nullable = false)
  private Integer stockQty;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
