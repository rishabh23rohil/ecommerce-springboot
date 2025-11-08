package com.rishabh.ecom.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

  @GetMapping("/api/v1/healthz")
  public ResponseEntity<Map<String, Object>> healthz() {
    return ResponseEntity.ok(Map.of(
        "service", "ecommerce-springboot",
        "status", "OK"
    ));
  }
}
