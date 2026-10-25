CREATE TABLE password_reset_requests (
    email VARCHAR(255) PRIMARY KEY,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    resend_after DATETIME NOT NULL,
    attempts INT NOT NULL DEFAULT 0
);
