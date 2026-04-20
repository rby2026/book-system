-- 创建数据库
CREATE DATABASE IF NOT EXISTS book_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE book_system;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role INT DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员',
    status INT DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图书表
CREATE TABLE IF NOT EXISTS t_book (
    id BIGINT AUTO_INCREMENT COMMENT '图书ID',
    title VARCHAR(100) NOT NULL COMMENT '图书名称',
    author VARCHAR(50) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    isbn VARCHAR(20) NOT NULL COMMENT 'ISBN编号',
    category VARCHAR(50) COMMENT '分类',
    location VARCHAR(50) COMMENT '位置',
    status INT DEFAULT 1 COMMENT '图书状态：0-不可借阅，1-可借阅',
    description TEXT COMMENT '图书简介',
    cover_url VARCHAR(255) COMMENT '图书封面',
    total_count INT DEFAULT 0 COMMENT '总数量',
    available_count INT DEFAULT 0 COMMENT '可借数量',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅表
CREATE TABLE IF NOT EXISTS t_borrow (
    id BIGINT AUTO_INCREMENT COMMENT '借阅ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    borrow_time DATETIME NOT NULL COMMENT '借阅时间',
    return_time DATETIME NOT NULL COMMENT '应还时间',
    actual_return_time DATETIME COMMENT '实际归还时间',
    renew_count INT DEFAULT 0 COMMENT '续借次数',
    status INT DEFAULT 0 COMMENT '借阅状态：0-借阅中，1-已归还，2-已逾期未还，3-已逾期已还',
    fine DECIMAL(10,2) DEFAULT 0 COMMENT '罚款金额（元）',
    fine_status INT DEFAULT 0 COMMENT '罚款状态：0-无需罚款，1-未缴纳，2-已缴纳',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅表';

-- 借阅规则表
CREATE TABLE IF NOT EXISTS t_borrow_rule (
    id BIGINT AUTO_INCREMENT COMMENT '规则ID',
    name VARCHAR(50) NOT NULL COMMENT '规则名称',
    borrow_days INT DEFAULT 30 COMMENT '借阅天数',
    max_renew_count INT DEFAULT 1 COMMENT '最大续借次数',
    renew_days INT DEFAULT 15 COMMENT '每次续借天数',
    max_borrow_count INT DEFAULT 5 COMMENT '最大借阅数量',
    fine_per_day DECIMAL(10,2) DEFAULT 0.5 COMMENT '逾期罚款（元/天）',
    status INT DEFAULT 0 COMMENT '规则状态：0-禁用，1-启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅规则表';
-- 初始化借阅规则
INSERT INTO t_borrow_rule (name, borrow_days, max_renew_count, renew_days, max_borrow_count, fine_per_day, status, create_time, update_time)
VALUES ('默认规则', 30, 1, 15, 5, 0.5, 1, NOW(), NOW());

-- 系统日志表
CREATE TABLE IF NOT EXISTS t_log (
    id BIGINT AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(64) COMMENT 'IP地址',
    status INT DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    error_msg TEXT COMMENT '错误信息',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    execution_time BIGINT COMMENT '执行时长(毫秒)',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 插入示例用户数据
INSERT INTO t_user (username, password, real_name, phone, email, role, status, create_time, update_time) VALUES
('user1', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '张三', '13800138001', 'zhangsan@example.com', 0, 1, NOW(), NOW()),
('user2', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '李四', '13800138002', 'lisi@example.com', 0, 1, NOW(), NOW()),
('admin', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '管理员', '13800138003', 'admin@qq.com', 1, 1, NOW(), NOW()),
('librarian', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '王五', '13800138004', 'wangwu@example.com', 1, 1, NOW(), NOW());

-- 插入示例图书数据
INSERT INTO t_book (title, author, publisher, isbn, category, location, status, description, total_count, available_count, create_time, update_time) VALUES
('Java编程思想', 'Bruce Eckel', '机械工业出版社', '9787111213826', 'TECHNOLOGY', 'A区-1架-1列', 1, 'Java编程的经典著作', 5, 3, NOW(), NOW()),
('算法导论', 'Thomas H.Cormen', '机械工业出版社', '9787111187776', 'TECHNOLOGY', 'A区-1架-2列', 1, '计算机科学领域的经典教材', 3, 2, NOW(), NOW()),
('百年孤独', '加西亚·马尔克斯', '南海出版公司', '9787544253994', 'LITERATURE', 'B区-2架-1列', 1, '魔幻现实主义文学代表作', 2, 1, NOW(), NOW()),
('三体', '刘慈欣', '重庆出版社', '9787536692930', 'LITERATURE', 'B区-2架-2列', 1, '中国科幻文学代表作', 4, 4, NOW(), NOW()),
('人类简史', '尤瓦尔·赫拉利', '中信出版社', '9787508647357', 'HISTORY', 'C区-1架-1列', 1, '从人类进化的角度回顾历史', 3, 2, NOW(), NOW());

-- 插入示例借阅数据
INSERT INTO t_borrow (user_id, book_id, borrow_time, return_time, actual_return_time, renew_count, status, fine, fine_status, create_time, update_time) VALUES
(2, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 30 DAY), NULL, 0, 0, 0, 0, NOW(), NOW()),
(2, 2, DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, 0, 2, 5.00, 1, NOW(), NOW()),
(4, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 30 DAY), NULL, 1, 0, 0, 0, NOW(), NOW()),
(2, 4, DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 0, 1, 0, 0, NOW(), NOW());

-- 插入示例借阅规则
INSERT INTO t_borrow_rule (name, borrow_days, max_renew_count, renew_days, max_borrow_count, fine_per_day, status, create_time, update_time) VALUES
('学生规则', 15, 1, 7, 3, 0.5, 0, NOW(), NOW()),
('教师规则', 30, 2, 15, 10, 0.5, 0, NOW(), NOW());

-- 插入示例系统日志
INSERT INTO t_log (user_id, username, operation, method, params, ip, status, operation_time, execution_time) VALUES
(1, 'admin', '用户登录', 'com.example.booksystem.controller.UserController.login', '{"username":"admin"}', '127.0.0.1', 1, NOW(), 105),
(2, 'user1', '借阅图书', 'com.example.booksystem.controller.BorrowController.borrowBook', '{"bookId":1}', '127.0.0.1', 1, NOW(), 86),
(3, 'librarian', '添加图书', 'com.example.booksystem.controller.BookController.addBook', '{"title":"Java编程思想"}', '127.0.0.1', 1, NOW(), 150),
(2, 'user1', '续借图书', 'com.example.booksystem.controller.BorrowController.renewBook', '{"borrowId":1}', '127.0.0.1', 1, NOW(), 78),
(1, 'admin', '修改规则', 'com.example.booksystem.controller.RuleController.updateRule', '{"id":1}', '127.0.0.1', 1, NOW(), 92); 