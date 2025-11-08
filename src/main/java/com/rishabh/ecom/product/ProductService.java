package com.rishabh.ecom.product;

import com.rishabh.ecom.product.dto.ProductDtos;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {
  private final ProductRepository repo;

  public ProductService(ProductRepository repo) {
    this.repo = repo;
  }

  @CacheEvict(value = "productById", key = "#result.id", condition = "#result != null")
  public Product create(ProductDtos.Create in) {
    if (repo.existsBySku(in.sku())) throw new IllegalStateException("SKU already exists");
    Product p = Product.builder()
        .name(in.name())
        .description(in.description())
        .price(in.price())
        .sku(in.sku())
        .stockQty(in.stockQty())
        .build();
    return repo.save(p);
  }

  @Transactional(readOnly = true)
  public List<Product> list() { return repo.findAll(); }

  @Transactional(readOnly = true)
  @Cacheable(value = "productById", key = "#id")
  public Product get(Long id) { return repo.findById(id).orElseThrow(); }

  @CacheEvict(value = "productById", key = "#id")
  public Product update(Long id, ProductDtos.Update in) {
    Product p = repo.findById(id).orElseThrow();
    if (in.name() != null) p.setName(in.name());
    if (in.description() != null) p.setDescription(in.description());
    if (in.price() != null) p.setPrice(in.price());
    if (in.stockQty() != null) p.setStockQty(in.stockQty());
    return p;
  }

  @CacheEvict(value = "productById", key = "#id")
  public void delete(Long id) { repo.deleteById(id); }
}
