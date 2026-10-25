-- Keep the original sample products, but do not count them as recent arrivals.
UPDATE products
SET last_restocked_at = UTC_TIMESTAMP() - INTERVAL 2 MONTH;

-- Add 997 products so the initial catalog contains exactly 1,000 watches.
-- Rows 1-40 are restocked within the last 28 days; the remaining rows are older.
INSERT INTO products (
  category_id,
  name,
  brand,
  description,
  movement_type,
  price,
  stock,
  is_active,
  image_url,
  created_at,
  last_restocked_at
)
WITH RECURSIVE watch_numbers (number) AS (
  SELECT 1
  UNION ALL
  SELECT number + 1
  FROM watch_numbers
  WHERE number < 997
),
watch_data AS (
  SELECT
    number,
    MOD(number - 1, 3) + 1 AS category_id,
    CASE MOD(number - 1, 12)
      WHEN 0 THEN 'Seiko'
      WHEN 1 THEN 'Casio'
      WHEN 2 THEN 'Orient'
      WHEN 3 THEN 'Citizen'
      WHEN 4 THEN 'Tissot'
      WHEN 5 THEN 'Fossil'
      WHEN 6 THEN 'Daniel Wellington'
      WHEN 7 THEN 'Omega'
      WHEN 8 THEN 'Longines'
      WHEN 9 THEN 'Garmin'
      WHEN 10 THEN 'Apple'
      ELSE 'Samsung'
    END AS brand
  FROM watch_numbers
),
dated_watch_data AS (
  SELECT
    number,
    category_id,
    brand,
    CASE
      WHEN number <= 40
        THEN UTC_TIMESTAMP() - INTERVAL MOD(number - 1, 28) DAY
      ELSE UTC_TIMESTAMP() - INTERVAL (60 + MOD(number, 670)) DAY
    END AS catalog_date
  FROM watch_data
)
SELECT
  category_id,
  CONCAT(brand, ' Collection ', LPAD(number, 4, '0')),
  brand,
  'Mau dong ho chinh hang, thiet ke hien dai, phu hop de deo hang ngay va lam qua tang.',
  CASE
    WHEN category_id = 3 THEN 'SMART'
    WHEN MOD(number, 2) = 0 THEN 'AUTOMATIC'
    ELSE 'QUARTZ'
  END,
  1200000 + MOD(number * 137000, 48800000),
  5 + MOD(number * 7, 96),
  TRUE,
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80',
  catalog_date,
  catalog_date
FROM dated_watch_data;
