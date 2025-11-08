package com.rishabh.ecom.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_sku", columnList = "sku", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank @Size(max = 120)
  private String name;

  @Size(max = 1000)
  private String description;

  @NotNull @DecimalMin(value = "0.00")
  @Column(precision = 14, scale = 2, nullable = false)
  private BigDecimal price;

  @NotBlank @Size(max = 40)
  @Column(nullable = false, unique = true)
  private String sku;

  @NotNull @Min(0)
  @Column(nullable = false)
  private Integer stockQty;

  @NotNull
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
