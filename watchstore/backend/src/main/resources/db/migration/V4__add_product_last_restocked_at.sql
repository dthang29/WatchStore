-- Thời điểm nhập thêm hàng; dùng để hiển thị "Hàng mới về".
ALTER TABLE products
  ADD COLUMN last_restocked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_at,
  ADD INDEX idx_products_active_last_restocked_at (is_active, last_restocked_at);

-- Dữ liệu cũ chưa có lịch sử nhập hàng, nên lấy thời điểm tạo sản phẩm làm mốc ban đầu.
UPDATE products
SET last_restocked_at = created_at;
