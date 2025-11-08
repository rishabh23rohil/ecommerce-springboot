CREATE TABLE IF NOT EXISTS products (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku           VARCHAR(64)  NOT NULL,
  name          VARCHAR(255) NOT NULL,
  description   TEXT         NULL,
  price         DECIMAL(10,2) NOT NULL,           -- changed
  stock_qty     INT NOT NULL DEFAULT 0,           -- add if your entity has stockQty
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_products_sku (sku),
  KEY idx_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
