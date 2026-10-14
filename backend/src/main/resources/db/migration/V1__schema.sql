CREATE TABLE admin_users (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT chk_admin_role CHECK (role = 'ADMIN')
);
CREATE TABLE categories (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE, description VARCHAR(500),
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL
);
CREATE TABLE products (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(150) NOT NULL, brand VARCHAR(100),
 category_id BIGINT NOT NULL, description VARCHAR(2000) NOT NULL, image_url VARCHAR(500),
 price DECIMAL(12,2), original_price DECIMAL(12,2), availability VARCHAR(20) NOT NULL DEFAULT 'ENQUIRE',
 featured BOOLEAN NOT NULL DEFAULT FALSE, demo BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
 CONSTRAINT chk_price CHECK (price IS NULL OR price >= 0),
 CONSTRAINT chk_original_price CHECK (original_price IS NULL OR (price IS NOT NULL AND original_price >= price)),
 CONSTRAINT chk_availability CHECK (availability IN ('ENQUIRE','AVAILABLE','OUT_OF_STOCK','PRE_ORDER'))
);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_featured ON products(featured);
CREATE INDEX idx_product_created ON products(created_at);
CREATE INDEX idx_product_price ON products(price);
CREATE TABLE offers (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(150) NOT NULL, description VARCHAR(2000) NOT NULL,
 image_url VARCHAR(500), product_id BIGINT, expires_on DATE NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT fk_offer_product FOREIGN KEY (product_id) REFERENCES products(id)
);
CREATE INDEX idx_offer_expiry ON offers(active,expires_on);
CREATE TABLE banners (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(150) NOT NULL, description VARCHAR(1000) NOT NULL,
 image_url VARCHAR(500), expires_on DATE, active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL
);
CREATE INDEX idx_banner_active ON banners(active,expires_on);
CREATE TABLE faqs (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, question VARCHAR(300) NOT NULL, answer VARCHAR(2000) NOT NULL,
 display_order INT NOT NULL DEFAULT 0, created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT chk_faq_order CHECK (display_order >= 0)
);
CREATE TABLE enquiries (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL,
 email VARCHAR(254), message VARCHAR(2000) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'NEW',
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT chk_enquiry_status CHECK (status IN ('NEW','IN_PROGRESS','CLOSED'))
);
CREATE INDEX idx_enquiry_status ON enquiries(status,created_at);
CREATE TABLE repair_enquiries (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL,
 email VARCHAR(254), message VARCHAR(2000) NOT NULL, device VARCHAR(150) NOT NULL, service VARCHAR(100) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'NEW', created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT chk_repair_status CHECK (status IN ('NEW','IN_PROGRESS','CLOSED'))
);
CREATE INDEX idx_repair_status ON repair_enquiries(status,created_at);
CREATE TABLE website_settings (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, business_name VARCHAR(100) NOT NULL,
 phone VARCHAR(20) NOT NULL, whatsapp VARCHAR(20) NOT NULL, email VARCHAR(254) NOT NULL,
 address VARCHAR(500) NOT NULL, business_hours VARCHAR(1000) NOT NULL,
 tagline VARCHAR(200) NOT NULL, announcement VARCHAR(300) NOT NULL,
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL
);
