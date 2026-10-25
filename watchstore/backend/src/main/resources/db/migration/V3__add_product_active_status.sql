-- Xóa sản phẩm theo kiểu mềm: giữ dữ liệu và lịch sử đơn hàng, chỉ ẩn khỏi cửa hàng.
ALTER TABLE products
  ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER stock,
  ADD INDEX idx_products_is_active_created_at (is_active, created_at);
