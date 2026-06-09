-- KRITHE STORE Database Schema
CREATE DATABASE IF NOT EXISTS krithe_store;
USE krithe_store;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Products
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    discount_price DECIMAL(10,2),
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    images TEXT,
    rating DECIMAL(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    featured BOOLEAN DEFAULT FALSE,
    trending BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    size VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (user_id, product_id, size)
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','PAID','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PENDING',
    shipping_name VARCHAR(100),
    shipping_email VARCHAR(150),
    shipping_phone VARCHAR(20),
    shipping_address TEXT,
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_pincode VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order Items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    size VARCHAR(10),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    razorpay_order_id VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    method VARCHAR(50),
    status ENUM('CREATED','SUCCESS','FAILED') DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Wishlist
CREATE TABLE IF NOT EXISTS wishlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_wishlist (user_id, product_id)
);

-- Recently Viewed
CREATE TABLE IF NOT EXISTS recently_viewed (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Sample Products
INSERT INTO products (name, description, brand, category, price, discount_price, stock, image_url, rating, review_count, featured, trending) VALUES
('Air Monarch Elite', 'Premium leather sneakers with cushioned sole and breathable mesh upper. Perfect for everyday luxury.', 'Nike', 'Sneakers', 5.00, 4.00, 50, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600', 4.8, 234, TRUE, TRUE),
('Velocity Runner Pro', 'Lightweight performance running shoes with responsive foam and carbon plate technology.', 'Adidas', 'Running', 5.00, 3.00, 35, 'https://images.unsplash.com/photo-1606107557192-0a74c4c4d6ac?w=600', 4.9, 189, TRUE, TRUE),
('Classic Oxford Heritage', 'Handcrafted Italian leather oxford shoes for formal occasions and boardroom elegance.', 'Gucci', 'Formal', 4.00, 3.00, 20, 'https://images.unsplash.com/photo-1614252239476-9523f5b9d0e2?w=600', 4.7, 156, TRUE, FALSE),
('Urban Street Flex', 'Street-style high tops with premium suede and iconic silhouette design.', 'Jordan', 'Sneakers', 5.00, 4.00, 40, 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600', 4.6, 312, FALSE, TRUE),
('Trail Blazer X', 'All-terrain hiking boots with waterproof membrane and Vibram outsole.', 'Salomon', 'Outdoor', 3.00, 2.00, 25, 'https://images.unsplash.com/photo-1525966222134-fcfa99b8d077?w=600', 4.5, 98, FALSE, TRUE),
('Slip-On Luxe', 'Minimalist slip-on loafers in supple calfskin leather with memory foam insole.', 'Tod''s', 'Casual', 4.00, 3.00, 30, 'https://images.unsplash.com/photo-1533867610401-7e5b1e0e0b0f?w=600', 4.8, 87, TRUE, FALSE),
('Court Master 88', 'Retro basketball-inspired sneakers with premium leather and gum sole.', 'New Balance', 'Sneakers', 2.00, 1.00, 60, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600', 4.4, 445, FALSE, TRUE),
('Eclipse Night Runner', 'Reflective running shoes with night-safe design and cloud cushioning.', 'Puma', 'Running', 3.00, 2.00, 45, 'https://images.unsplash.com/photo-1605348531061-4c375a0c4b8c?w=600', 4.3, 201, FALSE, FALSE),
('Derby Classic Black', 'Timeless black derby shoes with Goodyear welt construction.', 'Church''s', 'Formal', 5.00, 4.00, 15, 'https://images.unsplash.com/photo-1614252369475-531eba2f5f8a?w=600', 4.9, 67, TRUE, FALSE),
('Wave Rider Infinity', 'Maximum cushioning daily trainer with engineered mesh upper.', 'Mizuno', 'Running', 3.00, 2.00, 38, 'https://images.unsplash.com/photo-1606107557192-0a74c4c4d6ac?w=600', 4.6, 178, FALSE, TRUE),
('Chelsea Boot Noir', 'Sleek Chelsea boots in polished leather with elastic side panels.', 'Dr. Martens', 'Boots', 4.00, 3.00, 28, 'https://images.unsplash.com/photo-1638247025967-f4e8f9640a60?w=600', 4.7, 134, TRUE, TRUE),
('Retro Wave 90s', 'Chunky retro sneakers inspired by 90s street culture with bold colorways.', 'Reebok', 'Sneakers', 1.00, 1.00, 55, 'https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=600', 4.2, 289, FALSE, TRUE);
