-- =====================================================================
--  WATCH SHOP DATABASE (MySQL 8.0+) - bản gọn cho dự án nhỏ
--  Login: Email/Password hoặc Google OAuth, xác thực bằng JWT
--  Nguồn: watch_shop.sql (bỏ DROP/CREATE/USE DATABASE vì Flyway chạy trong DB đã chọn)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. USERS (gộp luôn thông tin Google vào đây)
-- ---------------------------------------------------------------------
CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NULL,          -- NULL nếu chỉ login bằng Google
  google_id     VARCHAR(255) NULL UNIQUE,   -- claim "sub" trong Google id_token
  full_name     VARCHAR(150) NOT NULL,
  phone         VARCHAR(20)  NULL,
  address       VARCHAR(255) NULL,
  avatar_url    VARCHAR(500) NULL,
  role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 2. REFRESH TOKEN cho JWT (access token không cần lưu)
-- ---------------------------------------------------------------------
CREATE TABLE refresh_tokens (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  token      VARCHAR(500) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 3. SẢN PHẨM
-- ---------------------------------------------------------------------
CREATE TABLE categories (
  id   INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE          -- Đồng hồ nam, nữ, thông minh...
);

CREATE TABLE products (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  category_id   INT NOT NULL,
  name          VARCHAR(255) NOT NULL,
  brand         VARCHAR(100) NOT NULL,       -- Casio, Seiko, Orient...
  description   TEXT NULL,
  movement_type ENUM('QUARTZ','AUTOMATIC','SMART') NOT NULL,
  price         DECIMAL(12,2) NOT NULL,
  stock         INT NOT NULL DEFAULT 0,
  image_url     VARCHAR(500) NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ---------------------------------------------------------------------
-- 4. GIỎ HÀNG
-- ---------------------------------------------------------------------
CREATE TABLE cart_items (
  user_id    INT NOT NULL,
  product_id INT NOT NULL,
  quantity   INT NOT NULL DEFAULT 1,
  PRIMARY KEY (user_id, product_id),
  FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 5. ĐƠN HÀNG
-- ---------------------------------------------------------------------
CREATE TABLE orders (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  user_id          INT NOT NULL,
  receiver_name    VARCHAR(150) NOT NULL,
  receiver_phone   VARCHAR(20)  NOT NULL,
  shipping_address VARCHAR(255) NOT NULL,
  total_amount     DECIMAL(12,2) NOT NULL,
  payment_method   ENUM('COD','BANK_TRANSFER') NOT NULL DEFAULT 'COD',
  status           ENUM('PENDING','CONFIRMED','SHIPPING','DELIVERED','CANCELLED')
                   NOT NULL DEFAULT 'PENDING',
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  order_id   INT NOT NULL,
  product_id INT NOT NULL,
  quantity   INT NOT NULL,
  price      DECIMAL(12,2) NOT NULL,         -- giá tại thời điểm mua
  FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ---------------------------------------------------------------------
-- 6. DỮ LIỆU MẪU
-- ---------------------------------------------------------------------
-- Tài khoản admin KHÔNG insert ở đây (hash placeholder không đăng nhập được).
-- AdminSeeder tạo admin với hash BCrypt thật từ biến ADMIN_EMAIL / ADMIN_PASSWORD.

INSERT INTO categories (name) VALUES
  ('Đồng hồ nam'), ('Đồng hồ nữ'), ('Đồng hồ thông minh');

INSERT INTO products (category_id, name, brand, movement_type, price, stock, image_url) VALUES
  (1, 'Seiko 5 Sports SRPD55K1', 'Seiko',  'AUTOMATIC', 7500000, 15, '/images/srpd55k1.jpg'),
  (1, 'Casio G-Shock GA-2100',   'Casio',  'QUARTZ',    3200000, 30, '/images/ga2100.jpg'),
  (2, 'Tissot PR 100 Lady',      'Tissot', 'QUARTZ',    9800000,  5, '/images/tissot-pr100.jpg');
