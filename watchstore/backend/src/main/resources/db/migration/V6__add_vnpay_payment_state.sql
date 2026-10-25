ALTER TABLE orders
    MODIFY COLUMN payment_method ENUM('COD','BANK_TRANSFER','VNPAY') NOT NULL DEFAULT 'COD',
    ADD COLUMN note VARCHAR(500) NULL,
    ADD COLUMN payment_status ENUM('PENDING','PAID','FAILED') NOT NULL DEFAULT 'PENDING',
    ADD COLUMN payment_reference VARCHAR(100) NULL,
    ADD CONSTRAINT uq_orders_payment_reference UNIQUE (payment_reference);
