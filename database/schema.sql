CREATE DATABASE IF NOT EXISTS pairstudy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pairstudy;
-- Serialize rare pair membership changes to prevent cross-invite races.
CREATE TABLE IF NOT EXISTS pair_lock (id INT PRIMARY KEY) ENGINE=InnoDB;
INSERT IGNORE INTO pair_lock(id) VALUES (1);
CREATE TABLE IF NOT EXISTS `user` (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(32) NOT NULL UNIQUE,
 password VARCHAR(100) NOT NULL,
 nickname VARCHAR(40) NOT NULL,
 avatar VARCHAR(255) NOT NULL DEFAULT '',
 group_id BIGINT NULL,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS pair_group (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_a_id BIGINT NOT NULL UNIQUE,
 user_b_id BIGINT NULL UNIQUE,
 invite_code VARCHAR(24) NOT NULL UNIQUE,
 created_at DATETIME(6) NOT NULL,
 bound_at DATETIME(6) NULL,
 CONSTRAINT fk_pair_a FOREIGN KEY(user_a_id) REFERENCES `user`(id),
 CONSTRAINT fk_pair_b FOREIGN KEY(user_b_id) REFERENCES `user`(id),
 CONSTRAINT different_members CHECK (user_b_id IS NULL OR user_a_id <> user_b_id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS category (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 group_id BIGINT NOT NULL,
 name VARCHAR(40) NOT NULL,
 icon_name VARCHAR(32) NOT NULL DEFAULT 'book',
 sort_order INT NOT NULL DEFAULT 0,
 archived BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,
 INDEX idx_category_group(group_id,archived,sort_order),
 FOREIGN KEY(group_id) REFERENCES pair_group(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS checkin (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 group_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 category_id BIGINT NOT NULL,
 content TEXT NOT NULL,
 request_id VARCHAR(36) NOT NULL,
 effective_date DATE NOT NULL,
 created_at DATETIME(6) NOT NULL,
 UNIQUE KEY uk_checkin_request(user_id,request_id),
 INDEX idx_checkin_feed(group_id,created_at,id),
 INDEX idx_checkin_day(group_id,effective_date,user_id),
 INDEX idx_checkin_category(group_id,category_id,created_at),
 FOREIGN KEY(group_id) REFERENCES pair_group(id),
 FOREIGN KEY(user_id) REFERENCES `user`(id),
 FOREIGN KEY(category_id) REFERENCES category(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS uploaded_image (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 group_id BIGINT NOT NULL,
 filename VARCHAR(80) NOT NULL UNIQUE,
 image_url VARCHAR(255) NOT NULL,
 media_type VARCHAR(32) NOT NULL,
 claimed BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(6) NOT NULL,
 INDEX idx_upload_owner(user_id,group_id),
 FOREIGN KEY(user_id) REFERENCES `user`(id),
 FOREIGN KEY(group_id) REFERENCES pair_group(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS checkin_image (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 checkin_id BIGINT NOT NULL,
 upload_id BIGINT NOT NULL UNIQUE,
 image_url VARCHAR(255) NOT NULL,
 sort_order INT NOT NULL,
 created_at DATETIME(6) NOT NULL,
 INDEX idx_image_checkin(checkin_id,sort_order),
 FOREIGN KEY(checkin_id) REFERENCES checkin(id) ON DELETE CASCADE,
 FOREIGN KEY(upload_id) REFERENCES uploaded_image(id)
) ENGINE=InnoDB;
