package com.rishabh.ecom.product;

import com.rishabh.ecom.product.dto.ProductDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService svc;
  public ProductController(ProductService svc) { this.svc = svc; }

  @PostMapping
  public ResponseEntity<Product> create(@Valid @RequestBody ProductDtos.Create in) {
    Product saved = svc.create(in);
    return ResponseEntity.created(URI.create("/api/v1/products/" + saved.getId())).body(saved);
  }

  @GetMapping
  public List<Product> list() { return svc.list(); }

  @GetMapping("/{id}")
  public Product get(@PathVariable Long id) { return svc.get(id); }

  @PatchMapping("/{id}")
  public Product update(@PathVariable Long id, @Valid @RequestBody ProductDtos.Update in) {
    return svc.update(id, in);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    svc.delete(id);
    return ResponseEntity.noContent().build();
  }
}
