package com.rishabh.ecom.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDtos {

  public record Create(
      @NotBlank @Size(max = 120) String name,
      @Size(max = 1000) String description,
      @NotNull @DecimalMin("0.00") BigDecimal price,
      @NotBlank @Size(max = 40) String sku,
      @NotNull @Min(0) Integer stockQty
  ) {}

  public record Update(
      @Size(max = 120) String name,
      @Size(max = 1000) String description,
      @DecimalMin("0.00") BigDecimal price,
      @Min(0) Integer stockQty
  ) {}
}
