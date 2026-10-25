CREATE TABLE pending_registrations (
    email VARCHAR(255) PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    resend_after DATETIME NOT NULL,
    attempts INT NOT NULL DEFAULT 0
);
