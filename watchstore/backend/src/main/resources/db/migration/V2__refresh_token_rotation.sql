-- Refresh token rotation + phát hiện token bị dùng lại:
-- token cũ không xoá mà đánh dấu thu hồi, để nhận ra khi nó bị dùng lại.
-- Cột refresh_tokens.token lưu SHA-256 (hex) của token, không lưu token gốc.
ALTER TABLE refresh_tokens
  ADD COLUMN revoked_at DATETIME NULL AFTER expires_at,
  ADD INDEX idx_refresh_tokens_expires_at (expires_at);
