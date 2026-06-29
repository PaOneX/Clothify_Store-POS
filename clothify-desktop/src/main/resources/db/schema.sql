CREATE DATABASE IF NOT EXISTS clothify_store_db;

USE clothify_store_db;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS notification_log;
DROP TABLE IF EXISTS return_item;
DROP TABLE IF EXISTS return_header;
DROP TABLE IF EXISTS order_payment;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS order_header;
DROP TABLE IF EXISTS inventory_log;
DROP TABLE IF EXISTS product_variant;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS discount;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS supplier;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS employee;

CREATE TABLE employee (
    employee_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(100),
    address       VARCHAR(255),
    hire_date     DATE NOT NULL,
    active        TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE `user` (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN', 'STAFF') NOT NULL,
    employee_id   INT,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until  TIMESTAMP NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

CREATE TABLE category (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(255)
);

CREATE TABLE supplier (
    supplier_id   INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(100),
    address       VARCHAR(255)
);

CREATE TABLE customer (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(100),
    address       VARCHAR(255),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active        TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE discount (
    discount_id   INT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(30) UNIQUE,
    name          VARCHAR(100) NOT NULL,
    type          ENUM('PERCENTAGE', 'FIXED') NOT NULL,
    value         DECIMAL(10, 2) NOT NULL,
    min_order     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    valid_from    DATE,
    valid_to      DATE,
    active        TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE product (
    product_id    INT AUTO_INCREMENT PRIMARY KEY,
    product_name  VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    image_path    VARCHAR(255) DEFAULT NULL,
    category_id   INT,
    supplier_id   INT,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
);

CREATE TABLE product_variant (
    variant_id    INT AUTO_INCREMENT PRIMARY KEY,
    product_id    INT NOT NULL,
    sku           VARCHAR(50) UNIQUE,
    size          VARCHAR(20),
    color         VARCHAR(30),
    barcode       VARCHAR(50) UNIQUE,
    price         DECIMAL(10, 2) NOT NULL,
    cost_price    DECIMAL(10, 2) NOT NULL DEFAULT 0,
    qty_on_hand   INT NOT NULL DEFAULT 0,
    active        TINYINT(1) NOT NULL DEFAULT 1,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

CREATE TABLE inventory_log (
    log_id        INT AUTO_INCREMENT PRIMARY KEY,
    variant_id    INT NOT NULL,
    change_qty    INT NOT NULL,
    reason        VARCHAR(50) NOT NULL,
    user_id       INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (variant_id) REFERENCES product_variant(variant_id),
    FOREIGN KEY (user_id) REFERENCES `user`(user_id)
);

CREATE TABLE order_header (
    order_id         INT AUTO_INCREMENT PRIMARY KEY,
    order_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cashier_id       INT NOT NULL,
    customer_id      INT,
    subtotal         DECIMAL(10, 2) NOT NULL,
    discount_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount_id      INT,
    tax              DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total            DECIMAL(10, 2) NOT NULL,
    payment_method   ENUM('CASH', 'CARD', 'BANK_TRANSFER', 'SPLIT') NOT NULL DEFAULT 'CASH',
    amount_received  DECIMAL(10, 2),
    change_given     DECIMAL(10, 2),
    status           ENUM('COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY (cashier_id) REFERENCES `user`(user_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (discount_id) REFERENCES discount(discount_id)
);

CREATE TABLE order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    variant_id    INT NOT NULL,
    qty           INT NOT NULL,
    unit_price    DECIMAL(10, 2) NOT NULL,
    line_total    DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES order_header(order_id),
    FOREIGN KEY (variant_id) REFERENCES product_variant(variant_id)
);

CREATE TABLE order_payment (
    payment_id    INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    method        ENUM('CASH', 'CARD', 'BANK_TRANSFER') NOT NULL,
    amount        DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES order_header(order_id)
);

CREATE TABLE invoice (
    invoice_id    INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL UNIQUE,
    invoice_no    VARCHAR(30) NOT NULL UNIQUE,
    generated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES order_header(order_id)
);

CREATE TABLE return_header (
    return_id     INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    return_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cashier_id    INT NOT NULL,
    reason        VARCHAR(255),
    refund_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    return_type   ENUM('REFUND', 'EXCHANGE') NOT NULL DEFAULT 'REFUND',
    FOREIGN KEY (order_id) REFERENCES order_header(order_id),
    FOREIGN KEY (cashier_id) REFERENCES `user`(user_id)
);

CREATE TABLE return_item (
    return_item_id INT AUTO_INCREMENT PRIMARY KEY,
    return_id      INT NOT NULL,
    variant_id     INT NOT NULL,
    qty            INT NOT NULL,
    unit_price     DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (return_id) REFERENCES return_header(return_id),
    FOREIGN KEY (variant_id) REFERENCES product_variant(variant_id)
);

CREATE TABLE audit_log (
    log_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT,
    action        VARCHAR(50) NOT NULL,
    entity_type   VARCHAR(50),
    entity_id     INT,
    details       VARCHAR(500),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id)
);

CREATE TABLE notification_log (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    type          VARCHAR(50) NOT NULL,
    message       VARCHAR(500) NOT NULL,
    dismissed     TINYINT(1) NOT NULL DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_header_date ON order_header(order_date);
CREATE INDEX idx_order_item_variant ON order_item(variant_id);
CREATE INDEX idx_invoice_no ON invoice(invoice_no);
CREATE INDEX idx_variant_barcode ON product_variant(barcode);
CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_audit_log_date ON audit_log(created_at);

-- Seed data
INSERT INTO employee (name, phone, email, address, hire_date, active)
VALUES ('System Admin', '0770000000', 'admin@clothify.com', 'Main Store', CURDATE(), 1);

INSERT INTO `user` (username, password_hash, role, employee_id)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 1);

INSERT INTO category (category_name, description) VALUES
('Men', 'Mens clothing'),
('Women', 'Womens clothing'),
('Kids', 'Kids clothing'),
('Shoes', 'Footwear'),
('Accessories', 'Bags and accessories');

INSERT INTO supplier (supplier_name, phone, email, address) VALUES
('Fashion Wholesale Ltd', '0112345678', 'orders@fashionwholesale.com', 'Colombo 03');

INSERT INTO customer (name, phone, email, address) VALUES
('Walk-in Customer', NULL, NULL, NULL),
('John Doe', '0771234567', 'john@example.com', 'Colombo 05');

INSERT INTO discount (code, name, type, value, min_order, valid_from, valid_to, active) VALUES
('WELCOME10', 'Welcome 10% Off', 'PERCENTAGE', 10.00, 1000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 1),
('FLAT500', 'Flat Rs 500 Off', 'FIXED', 500.00, 3000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 1);

INSERT INTO product (product_name, description, image_path, category_id, supplier_id) VALUES
('Classic T-Shirt', 'Cotton crew neck tee', 'images/products/tshirt.png', 1, 1),
('Denim Jeans', 'Slim fit blue jeans', 'images/products/jeans.png', 1, 1),
('Summer Dress', 'Floral print dress', 'images/products/dress.png', 2, 1);

INSERT INTO product_variant (product_id, sku, size, color, barcode, price, cost_price, qty_on_hand) VALUES
(1, 'TSH-S-WHT', 'S', 'White', '8901001001001', 1500.00, 800.00, 25),
(1, 'TSH-M-WHT', 'M', 'White', '8901001001002', 1500.00, 800.00, 30),
(1, 'TSH-L-BLK', 'L', 'Black', '8901001001003', 1500.00, 800.00, 20),
(2, 'JNS-30-BLU', '30', 'Blue', '8901001002001', 4500.00, 2500.00, 15),
(2, 'JNS-32-BLU', '32', 'Blue', '8901001002002', 4500.00, 2500.00, 12),
(3, 'DRS-S-FLR', 'S', 'Floral', '8901001003001', 5500.00, 3000.00, 10),
(3, 'DRS-M-FLR', 'M', 'Floral', '8901001003002', 5500.00, 3000.00, 8);
