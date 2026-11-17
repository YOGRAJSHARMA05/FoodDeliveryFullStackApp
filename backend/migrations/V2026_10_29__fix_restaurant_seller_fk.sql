-- ============================================================
--  Fix orphaned restaurant sellers
--  Run once against an existing food_delivery_db database.
--
--  Problem: restaurants.seller_id was added by Hibernate with an
--  implicit default of 0, and no FK existed, so every seeded
--  restaurant pointed at a non-existent user. Order placement then
--  tried to insert notifications.user_id = 0 and failed the FK.
--
--  This migration:
--   1. Creates a real SELLER account if one is missing.
--   2. Repoints every orphaned restaurant at that seller.
--   3. Makes seller_id NOT NULL.
--   4. Adds the missing restaurants -> users foreign key.
-- ============================================================

USE food_delivery_db;

-- 1) Demo seller login: seller@food.com / password123
INSERT INTO users (name, email, mobile_number, password, role)
SELECT 'Demo Seller', 'seller@food.com', '919000000001',
       '$2a$10$65VncgTbRO7c2ekoLQiNhOpJMBbcKuKpQgk0KUzIUu45tvmHU235m', 'SELLER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seller@food.com');

SET @seller_id := (SELECT id FROM users WHERE email = 'seller@food.com');

-- 2) Add seller_id for very old databases that predate the column.
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'restaurants'
    AND COLUMN_NAME = 'seller_id'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE restaurants ADD COLUMN seller_id BIGINT NULL',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Repair every row whose seller does not exist in users.
UPDATE restaurants r
SET r.seller_id = @seller_id
WHERE r.seller_id IS NULL
   OR NOT EXISTS (SELECT 1 FROM users u WHERE u.id = r.seller_id);

ALTER TABLE restaurants
    MODIFY COLUMN seller_id BIGINT NOT NULL;

-- 4) Enforce the relationship so this can never happen again.
SET @fk_exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'restaurants'
    AND CONSTRAINT_NAME = 'fk_restaurant_seller'
);
SET @ddl := IF(@fk_exists = 0,
  'ALTER TABLE restaurants ADD CONSTRAINT fk_restaurant_seller FOREIGN KEY (seller_id) REFERENCES users(id)',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verification: every restaurant must resolve to a real seller.
SELECT r.id, r.name, r.seller_id, u.email AS seller_email
FROM restaurants r
LEFT JOIN users u ON u.id = r.seller_id;
