CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng roles: chứa các vai trò (USER, ADMIN, MODERATOR)
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255)
);

-- Bảng user_roles: liên kết nhiều-nhiều giữa users và roles
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Thêm 3 vai trò cơ bản
INSERT INTO roles (name, description) VALUES
                                          ('USER', 'Người dùng thông thường'),
                                          ('ADMIN', 'Quản trị viên toàn quyền'),
                                          ('MODERATOR', 'Người kiểm duyệt nội dung');

-- Thêm một số user mẫu (mật khẩu đã được hash, ở đây mình để plain text cho dễ nhìn - trong thực tế bạn phải hash thật)
-- Lưu ý: Trong thực tế, password_hash phải là mã hash của mật khẩu (ví dụ bcrypt). Ở đây chỉ demo.
INSERT INTO users (email, password_hash, full_name, status) VALUES
                                                                ('user1@gmail.com', 'hashed_password_123', 'Nguyễn Văn A', 'ACTIVE'),
                                                                ('admin@gmail.com', 'hashed_password_456', 'Trần Thị B', 'ACTIVE'),
                                                                ('moderator@gmail.com', 'hashed_password_789', 'Lê Văn C', 'ACTIVE');

-- Gán role cho từng user
-- user1 -> USER
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1);  -- user_id=1, role_id=1 (USER)

-- admin -> ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES
    (2, 2);  -- user_id=2, role_id=2 (ADMIN)

-- moderator -> MODERATOR
INSERT INTO user_roles (user_id, role_id) VALUES
    (3, 3);  -- user_id=3, role_id=3 (MODERATOR)

-- (Tùy chọn) Bạn có thể cho user1 vừa là USER vừa là MODERATOR để thử nhiều role
-- INSERT INTO user_roles (user_id, role_id) VALUES (1, 3);

