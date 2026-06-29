USE clothify_store_db;

ALTER TABLE order_header
    ADD COLUMN IF NOT EXISTS order_source ENUM('POS', 'ONLINE') NOT NULL DEFAULT 'POS' AFTER status;

ALTER TABLE order_header
    MODIFY COLUMN status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED';

INSERT INTO employee (name, phone, email, address, hire_date, active)
SELECT 'Web Orders', '0000000000', 'web@clothify.com', 'Online', CURDATE(), 1
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE name = 'Web Orders');

INSERT INTO `user` (username, password_hash, role, employee_id)
SELECT 'web_orders', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'STAFF',
       (SELECT employee_id FROM employee WHERE name = 'Web Orders' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'web_orders');
