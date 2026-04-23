-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role INT DEFAULT 0,
    user_type INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id),
    UNIQUE (username)
);

-- 图书表
CREATE TABLE IF NOT EXISTS t_book (
    id BIGINT AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(50),
    publisher VARCHAR(100),
    isbn VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    sub_category VARCHAR(50),
    location VARCHAR(50),
    status INT DEFAULT 1,
    description TEXT,
    cover_url VARCHAR(255),
    total_count INT DEFAULT 0,
    available_count INT DEFAULT 0,
    borrow_count INT DEFAULT 0,
    publish_date DATETIME,
    pages INT,
    price DECIMAL(10,2),
    language VARCHAR(20),
    version VARCHAR(20),
    call_number VARCHAR(50),
    stock_date DATETIME,
    last_borrow_date DATETIME,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id),
    UNIQUE (isbn)
);

-- 借阅表
CREATE TABLE IF NOT EXISTS t_borrow (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_time DATETIME NOT NULL,
    return_time DATETIME NOT NULL,
    actual_return_time DATETIME,
    renew_count INT DEFAULT 0,
    status INT DEFAULT 0,
    fine DECIMAL(10,2) DEFAULT 0,
    fine_status INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id)
);

-- 借阅规则表
CREATE TABLE IF NOT EXISTS t_borrow_rule (
    id BIGINT AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    borrow_days INT DEFAULT 30,
    max_renew_count INT DEFAULT 1,
    renew_days INT DEFAULT 15,
    max_borrow_count INT DEFAULT 5,
    fine_per_day DECIMAL(10,2) DEFAULT 0.5,
    status INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id)
);

-- 系统日志表
CREATE TABLE IF NOT EXISTS t_log (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(50) NOT NULL,
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(64),
    status INT DEFAULT 1,
    error_msg TEXT,
    operation_time DATETIME NOT NULL,
    execution_time BIGINT,
    PRIMARY KEY (id)
);

-- 公告表
CREATE TABLE IF NOT EXISTS t_announcement (
    id BIGINT AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    publisher VARCHAR(50),
    publish_time DATETIME,
    status INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id)
);

-- 打卡记录表
CREATE TABLE IF NOT EXISTS t_check_in (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    create_time DATETIME,
    PRIMARY KEY (id),
    UNIQUE (user_id, check_in_date)
);

-- 违章信息表
CREATE TABLE IF NOT EXISTS t_violation (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    violation_type INT,
    description TEXT,
    book_id BIGINT,
    book_title VARCHAR(100),
    fine DECIMAL(10,2) DEFAULT 0,
    process_status INT DEFAULT 0,
    violation_time DATETIME,
    process_time DATETIME,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id)
);

-- 初始化借阅规则
INSERT INTO t_borrow_rule (name, borrow_days, max_renew_count, renew_days, max_borrow_count, fine_per_day, status, create_time, update_time)
VALUES ('默认规则', 30, 1, 15, 5, 0.5, 1, NOW(), NOW());

-- 插入示例用户数据
INSERT INTO t_user (username, password, real_name, phone, email, role, user_type, status, create_time, update_time) VALUES
('user1', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '张三', '13800138001', 'zhangsan@example.com', 0, 0, 1, NOW(), NOW()),
('user2', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '李四', '13800138002', 'lisi@example.com', 0, 1, 1, NOW(), NOW()),
('admin', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '管理员', '13800138003', 'admin@qq.com', 1, 1, 1, NOW(), NOW()),
('librarian', '$2a$10$KMZ4MTzx9wAML47PNh2yPOmzgFoBqjEYNGqfqYFtzNfpDUq3K4lxa', '王五', '13800138004', 'wangwu@example.com', 1, 1, 1, NOW(), NOW());

-- 插入示例图书数据
INSERT INTO t_book (title, author, publisher, isbn, category, sub_category, location, status, description, cover_url, total_count, available_count, borrow_count, publish_date, pages, price, language, version, call_number, stock_date, last_borrow_date, create_time, update_time) VALUES
('Java编程思想', 'Bruce Eckel', '机械工业出版社', '9787111213826', 'TECHNOLOGY', 'COMPUTER', 'A区-1架-1列', 1, 'Java编程的经典著作，全面介绍Java语言的核心概念和编程技巧', 'https://example.com/covers/java-thinking.jpg', 5, 3, 15, DATEADD('YEAR', -10, NOW()), 880, 99.00, 'Chinese', '4th', 'TP312JA/E21', DATEADD('YEAR', -8, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('算法导论', 'Thomas H.Cormen', '机械工业出版社', '9787111187776', 'TECHNOLOGY', 'COMPUTER', 'A区-1架-2列', 1, '计算机科学领域的经典教材，全面介绍算法设计与分析', 'https://example.com/covers/algorithm.jpg', 3, 2, 12, DATEADD('YEAR', -15, NOW()), 1312, 128.00, 'Chinese', '3rd', 'TP301.6/C73', DATEADD('YEAR', -10, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('百年孤独', '加西亚·马尔克斯', '南海出版公司', '9787544253994', 'LITERATURE', 'NOVEL', 'B区-2架-1列', 1, '魔幻现实主义文学代表作，讲述布恩迪亚家族七代人的传奇故事', 'https://example.com/covers/one-hundred-years.jpg', 2, 1, 8, DATEADD('YEAR', -50, NOW()), 360, 39.50, 'Chinese', '1st', 'I775.45/M12', DATEADD('YEAR', -5, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('三体', '刘慈欣', '重庆出版社', '9787536692930', 'LITERATURE', 'SCIENCE_FICTION', 'B区-2架-2列', 1, '中国科幻文学代表作，讲述人类文明与三体文明的接触', 'https://example.com/covers/three-body.jpg', 4, 4, 20, DATEADD('YEAR', -12, NOW()), 302, 32.00, 'Chinese', '1st', 'I247.55/L73', DATEADD('YEAR', -10, NOW()), NULL, NOW(), NOW()),
('人类简史', '尤瓦尔·赫拉利', '中信出版社', '9787508647357', 'HISTORY', 'WORLD_HISTORY', 'C区-1架-1列', 1, '从人类进化的角度回顾历史，探讨人类社会的发展历程', 'https://example.com/covers/sapiens.jpg', 3, 2, 14, DATEADD('YEAR', -8, NOW()), 440, 68.00, 'Chinese', '1st', 'K109/H32', DATEADD('YEAR', -6, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('活着', '余华', '作家出版社', '9787506365437', 'LITERATURE', 'NOVEL', 'B区-3架-1列', 1, '讲述了一个人一生的坎坷命运，反映了中国社会的变迁', 'https://example.com/covers/to-live.jpg', 3, 3, 10, DATEADD('YEAR', -25, NOW()), 191, 22.00, 'Chinese', '1st', 'I247.57/Y71', DATEADD('YEAR', -20, NOW()), NULL, NOW(), NOW()),
('时间简史', '史蒂芬·霍金', '湖南科学技术出版社', '9787535732309', 'SCIENCE', 'PHYSICS', 'D区-1架-1列', 1, '介绍宇宙学的基础知识，讲述时间和空间的本质', 'https://example.com/covers/brief-history.jpg', 2, 1, 7, DATEADD('YEAR', -20, NOW()), 256, 35.00, 'Chinese', '1st', 'P159/H94', DATEADD('YEAR', -15, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('小王子', '安托万·德·圣-埃克苏佩里', '人民文学出版社', '9787020002235', 'LITERATURE', 'FICTION', 'B区-4架-1列', 1, '一部充满哲理的童话，讲述小王子的星际之旅', 'https://example.com/covers/little-prince.jpg', 5, 4, 25, DATEADD('YEAR', -75, NOW()), 96, 19.00, 'Chinese', '1st', 'I565.88/S12', DATEADD('YEAR', -10, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('经济学原理', '曼昆', '北京大学出版社', '9787301150894', 'SOCIAL_SCIENCE', 'ECONOMICS', 'E区-1架-1列', 1, '经济学入门教材，介绍微观经济学和宏观经济学的基本原理', 'https://example.com/covers/economics.jpg', 3, 2, 8, DATEADD('YEAR', -10, NOW()), 540, 68.00, 'Chinese', '6th', 'F0-43/M23', DATEADD('YEAR', -8, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('心理学与生活', '理查德·格里格', '人民邮电出版社', '9787115201614', 'SOCIAL_SCIENCE', 'PSYCHOLOGY', 'E区-2架-1列', 1, '心理学入门教材，介绍心理学的基本概念和研究方法', 'https://example.com/covers/psychology.jpg', 2, 1, 6, DATEADD('YEAR', -8, NOW()), 650, 88.00, 'Chinese', '16th', 'B84-49/G71', DATEADD('YEAR', -5, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW());

-- 插入示例借阅数据
INSERT INTO t_borrow (user_id, book_id, borrow_time, return_time, actual_return_time, renew_count, status, fine, fine_status, create_time, update_time) VALUES
(2, 1, DATEADD('DAY', -20, NOW()), DATEADD('DAY', 10, NOW()), NULL, 0, 0, 0, 0, NOW(), NOW()),
(2, 2, DATEADD('DAY', -40, NOW()), DATEADD('DAY', -10, NOW()), NULL, 0, 2, 5.00, 1, NOW(), NOW()),
(4, 3, DATEADD('DAY', -10, NOW()), DATEADD('DAY', 20, NOW()), NULL, 1, 0, 0, 0, NOW(), NOW()),
(2, 4, DATEADD('DAY', -50, NOW()), DATEADD('DAY', -20, NOW()), DATEADD('DAY', -15, NOW()), 0, 1, 0, 0, NOW(), NOW());

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

-- 插入示例公告
INSERT INTO t_announcement (title, content, publisher, publish_time, status, create_time, update_time) VALUES
('图书馆开放时间调整通知', '尊敬的读者：\n\n因系统维护，本周末（周六、周日）图书馆开放时间调整为9:00-16:00，给您带来不便，敬请谅解。\n\n图书馆管理处\n' || DATEADD('DAY', -7, NOW()), '管理员', DATEADD('DAY', -7, NOW()), 1, DATEADD('DAY', -7, NOW()), DATEADD('DAY', -7, NOW())),
('图书借阅规则更新', '各位读者：\n\n为了更好地服务大家，我们更新了图书借阅规则：\n1. 借阅期限延长至30天\n2. 每卡最多可借5本图书\n3. 可续借1次，续借期限15天\n\n请大家遵守新规则，谢谢配合！\n\n图书馆管理处\n' || DATEADD('DAY', -14, NOW()), '管理员', DATEADD('DAY', -14, NOW()), 1, DATEADD('DAY', -14, NOW()), DATEADD('DAY', -14, NOW())),
('假期开放安排', '各位读者：\n\n五一假期期间，图书馆开放安排如下：\n- 5月1日-3日：闭馆\n- 5月4日起：正常开放\n\n祝大家假期愉快！\n\n图书馆管理处\n' || DATEADD('DAY', -30, NOW()), '管理员', DATEADD('DAY', -30, NOW()), 1, DATEADD('DAY', -30, NOW()), DATEADD('DAY', -30, NOW()));

-- 插入示例违章信息
INSERT INTO t_violation (user_id, username, violation_type, description, book_id, book_title, fine, process_status, violation_time, create_time, update_time) VALUES
(2, 'user1', 1, '未按时归还图书', 1, 'Java编程思想', 5.00, 0, DATEADD('DAY', -10, NOW()), DATEADD('DAY', -10, NOW()), DATEADD('DAY', -10, NOW())),
(3, 'user2', 2, '损坏图书封面', 3, '百年孤独', 10.00, 1, DATEADD('DAY', -15, NOW()), DATEADD('DAY', -15, NOW()), DATEADD('DAY', -10, NOW())),
(4, 'librarian', 1, '未按时归还图书', 2, '算法导论', 3.00, 0, DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW()));

-- 插入更多文学作品图书
INSERT INTO t_book (title, author, publisher, isbn, category, sub_category, location, status, description, cover_url, total_count, available_count, borrow_count, publish_date, pages, price, language, version, call_number, stock_date, last_borrow_date, create_time, update_time) VALUES
('红楼梦', '曹雪芹', '人民文学出版社', '9787020002207', 'LITERATURE', 'CLASSICAL', 'B区-1架-1列', 1, '中国古典文学四大名著之一', 'https://example.com/covers/hongloumeng.jpg', 5, 3, 25, DATEADD('YEAR', -250, NOW()), 1200, 128.00, 'Chinese', '1st', 'I242.47/C27', DATEADD('YEAR', -10, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('三国演义', '罗贯中', '人民文学出版社', '9787020002208', 'LITERATURE', 'CLASSICAL', 'B区-1架-2列', 1, '中国古典文学四大名著之一', 'https://example.com/covers/sanguoyanyi.jpg', 4, 2, 20, DATEADD('YEAR', -400, NOW()), 960, 98.00, 'Chinese', '1st', 'I242.47/L98', DATEADD('YEAR', -10, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('水浒传', '施耐庵', '人民文学出版社', '9787020002209', 'LITERATURE', 'CLASSICAL', 'B区-1架-3列', 1, '中国古典文学四大名著之一', 'https://example.com/covers/shuihuzhuan.jpg', 4, 3, 18, DATEADD('YEAR', -400, NOW()), 1000, 108.00, 'Chinese', '1st', 'I242.47/S54', DATEADD('YEAR', -10, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('西游记', '吴承恩', '人民文学出版社', '9787020002210', 'LITERATURE', 'CLASSICAL', 'B区-1架-4列', 1, '中国古典文学四大名著之一', 'https://example.com/covers/xiyouji.jpg', 5, 4, 22, DATEADD('YEAR', -400, NOW()), 880, 88.00, 'Chinese', '1st', 'I242.47/W86', DATEADD('YEAR', -10, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('诗经', '佚名', '中华书局', '9787101003048', 'LITERATURE', 'CLASSICAL', 'B区-2架-1列', 1, '中国古代诗歌总集', 'https://example.com/covers/shijing.jpg', 3, 2, 15, DATEADD('YEAR', -2500, NOW()), 400, 45.00, 'Chinese', '1st', 'I222.2/S59', DATEADD('YEAR', -8, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('楚辞', '屈原', '中华书局', '9787101003049', 'LITERATURE', 'CLASSICAL', 'B区-2架-2列', 1, '中国古代诗歌总集', 'https://example.com/covers/chuci.jpg', 3, 1, 12, DATEADD('YEAR', -2300, NOW()), 350, 42.00, 'Chinese', '1st', 'I222.3/Q82', DATEADD('YEAR', -8, NOW()), DATEADD('MONTH', -5, NOW()), NOW(), NOW()),
('论语', '孔子', '中华书局', '9787101003050', 'LITERATURE', 'CLASSICAL', 'B区-2架-3列', 1, '儒家经典著作', 'https://example.com/covers/lunyu.jpg', 4, 3, 18, DATEADD('YEAR', -2500, NOW()), 300, 38.00, 'Chinese', '1st', 'B222.2/K35', DATEADD('YEAR', -9, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('孟子', '孟子', '中华书局', '9787101003051', 'LITERATURE', 'CLASSICAL', 'B区-2架-4列', 1, '儒家经典著作', 'https://example.com/covers/mengzi.jpg', 3, 2, 14, DATEADD('YEAR', -2300, NOW()), 320, 40.00, 'Chinese', '1st', 'B222.5/M52', DATEADD('YEAR', -9, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('史记', '司马迁', '中华书局', '9787101003052', 'LITERATURE', 'CLASSICAL', 'B区-3架-1列', 1, '中国第一部纪传体通史', 'https://example.com/covers/shiji.jpg', 3, 1, 10, DATEADD('YEAR', -2000, NOW()), 1500, 198.00, 'Chinese', '1st', 'K204.2/S71', DATEADD('YEAR', -7, NOW()), DATEADD('MONTH', -6, NOW()), NOW(), NOW()),
('资治通鉴', '司马光', '中华书局', '9787101003053', 'LITERATURE', 'CLASSICAL', 'B区-3架-2列', 1, '中国古代编年体通史', 'https://example.com/covers/zizhitongjian.jpg', 2, 1, 8, DATEADD('YEAR', -1000, NOW()), 2000, 298.00, 'Chinese', '1st', 'K204.3/S51', DATEADD('YEAR', -7, NOW()), DATEADD('MONTH', -7, NOW()), NOW(), NOW()),
('唐诗三百首', '蘅塘退士', '中华书局', '9787101003054', 'LITERATURE', 'POETRY', 'B区-4架-1列', 1, '唐代诗歌选集', 'https://example.com/covers/tangshi.jpg', 4, 3, 16, DATEADD('YEAR', -200, NOW()), 380, 45.00, 'Chinese', '1st', 'I222.742/H37', DATEADD('YEAR', -6, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('宋词三百首', '朱孝臧', '中华书局', '9787101003055', 'LITERATURE', 'POETRY', 'B区-4架-2列', 1, '宋代词选集', 'https://example.com/covers/songci.jpg', 4, 2, 14, DATEADD('YEAR', -100, NOW()), 360, 42.00, 'Chinese', '1st', 'I222.844/Z88', DATEADD('YEAR', -6, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('元曲三百首', '任中敏', '中华书局', '9787101003056', 'LITERATURE', 'POETRY', 'B区-4架-3列', 1, '元代散曲选集', 'https://example.com/covers/yuanqu.jpg', 3, 2, 12, DATEADD('YEAR', -80, NOW()), 340, 38.00, 'Chinese', '1st', 'I222.94/R28', DATEADD('YEAR', -6, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('牡丹亭', '汤显祖', '人民文学出版社', '9787020002211', 'LITERATURE', 'DRAMA', 'B区-5架-1列', 1, '明代传奇剧本', 'https://example.com/covers/mudanting.jpg', 3, 1, 9, DATEADD('YEAR', -400, NOW()), 400, 48.00, 'Chinese', '1st', 'I237.2/T27', DATEADD('YEAR', -5, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('西厢记', '王实甫', '人民文学出版社', '9787020002212', 'LITERATURE', 'DRAMA', 'B区-5架-2列', 1, '元代杂剧剧本', 'https://example.com/covers/xixiangji.jpg', 3, 2, 11, DATEADD('YEAR', -700, NOW()), 380, 45.00, 'Chinese', '1st', 'I237.1/W46', DATEADD('YEAR', -5, NOW()), DATEADD('MONTH', -5, NOW()), NOW(), NOW()),
('雷雨', '曹禺', '人民文学出版社', '9787020002213', 'LITERATURE', 'DRAMA', 'B区-5架-3列', 1, '现代话剧', 'https://example.com/covers/leiyu.jpg', 4, 3, 15, DATEADD('YEAR', -80, NOW()), 200, 28.00, 'Chinese', '1st', 'I234/C27', DATEADD('YEAR', -4, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('茶馆', '老舍', '人民文学出版社', '9787020002214', 'LITERATURE', 'DRAMA', 'B区-5架-4列', 1, '现代话剧', 'https://example.com/covers/chaguan.jpg', 4, 2, 13, DATEADD('YEAR', -70, NOW()), 180, 25.00, 'Chinese', '1st', 'I234/L39', DATEADD('YEAR', -4, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('狂人日记', '鲁迅', '人民文学出版社', '9787020002215', 'LITERATURE', 'SHORT_STORY', 'B区-6架-1列', 1, '现代短篇小说', 'https://example.com/covers/kuangrenriji.jpg', 5, 3, 18, DATEADD('YEAR', -100, NOW()), 150, 22.00, 'Chinese', '1st', 'I246.7/L81', DATEADD('YEAR', -3, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('阿Q正传', '鲁迅', '人民文学出版社', '9787020002216', 'LITERATURE', 'SHORT_STORY', 'B区-6架-2列', 1, '现代短篇小说', 'https://example.com/covers/aqzhengzhuan.jpg', 5, 4, 20, DATEADD('YEAR', -100, NOW()), 180, 25.00, 'Chinese', '1st', 'I246.7/L81', DATEADD('YEAR', -3, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('祝福', '鲁迅', '人民文学出版社', '9787020002217', 'LITERATURE', 'SHORT_STORY', 'B区-6架-3列', 1, '现代短篇小说', 'https://example.com/covers/zhufu.jpg', 4, 3, 16, DATEADD('YEAR', -100, NOW()), 160, 23.00, 'Chinese', '1st', 'I246.7/L81', DATEADD('YEAR', -3, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('家', '巴金', '人民文学出版社', '9787020002218', 'LITERATURE', 'NOVEL', 'B区-7架-1列', 1, '现代长篇小说', 'https://example.com/covers/jia.jpg', 4, 2, 14, DATEADD('YEAR', -80, NOW()), 400, 45.00, 'Chinese', '1st', 'I246.5/B14', DATEADD('YEAR', -2, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('春', '巴金', '人民文学出版社', '9787020002219', 'LITERATURE', 'NOVEL', 'B区-7架-2列', 1, '现代长篇小说', 'https://example.com/covers/chun.jpg', 4, 3, 12, DATEADD('YEAR', -80, NOW()), 420, 48.00, 'Chinese', '1st', 'I246.5/B14', DATEADD('YEAR', -2, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('秋', '巴金', '人民文学出版社', '9787020002220', 'LITERATURE', 'NOVEL', 'B区-7架-3列', 1, '现代长篇小说', 'https://example.com/covers/qiu.jpg', 4, 2, 11, DATEADD('YEAR', -80, NOW()), 430, 49.00, 'Chinese', '1st', 'I246.5/B14', DATEADD('YEAR', -2, NOW()), DATEADD('MONTH', -5, NOW()), NOW(), NOW()),
('子夜', '茅盾', '人民文学出版社', '9787020002221', 'LITERATURE', 'NOVEL', 'B区-8架-1列', 1, '现代长篇小说', 'https://example.com/covers/ziye.jpg', 3, 2, 10, DATEADD('YEAR', -80, NOW()), 450, 50.00, 'Chinese', '1st', 'I246.5/M29', DATEADD('YEAR', -2, NOW()), DATEADD('MONTH', -6, NOW()), NOW(), NOW()),
('骆驼祥子', '老舍', '人民文学出版社', '9787020002222', 'LITERATURE', 'NOVEL', 'B区-8架-2列', 1, '现代长篇小说', 'https://example.com/covers/luotuoxiangzi.jpg', 5, 3, 18, DATEADD('YEAR', -80, NOW()), 300, 35.00, 'Chinese', '1st', 'I246.5/L39', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('四世同堂', '老舍', '人民文学出版社', '9787020002223', 'LITERATURE', 'NOVEL', 'B区-8架-3列', 1, '现代长篇小说', 'https://example.com/covers/sishitongtang.jpg', 3, 1, 8, DATEADD('YEAR', -70, NOW()), 800, 98.00, 'Chinese', '1st', 'I246.5/L39', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('围城', '钱钟书', '人民文学出版社', '9787020002224', 'LITERATURE', 'NOVEL', 'B区-9架-1列', 1, '现代长篇小说', 'https://example.com/covers/weicheng.jpg', 4, 2, 15, DATEADD('YEAR', -70, NOW()), 350, 40.00, 'Chinese', '1st', 'I246.5/Q27', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('活着', '余华', '作家出版社', '9787506365439', 'LITERATURE', 'NOVEL', 'B区-9架-2列', 1, '当代长篇小说', 'https://example.com/covers/huozhe.jpg', 5, 3, 20, DATEADD('YEAR', -30, NOW()), 191, 22.00, 'Chinese', '1st', 'I247.57/Y71', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('许三观卖血记', '余华', '作家出版社', '9787506365438', 'LITERATURE', 'NOVEL', 'B区-9架-3列', 1, '当代长篇小说', 'https://example.com/covers/xusanguan.jpg', 4, 2, 14, DATEADD('YEAR', -25, NOW()), 240, 28.00, 'Chinese', '1st', 'I247.57/Y71', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('平凡的世界', '路遥', '人民文学出版社', '9787020002225', 'LITERATURE', 'NOVEL', 'B区-10架-1列', 1, '当代长篇小说', 'https://example.com/covers/pingfandeshijie.jpg', 5, 3, 22, DATEADD('YEAR', -40, NOW()), 1200, 128.00, 'Chinese', '1st', 'I247.57/L84', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('白鹿原', '陈忠实', '人民文学出版社', '9787020002226', 'LITERATURE', 'NOVEL', 'B区-10架-2列', 1, '当代长篇小说', 'https://example.com/covers/bailuyuan.jpg', 4, 2, 16, DATEADD('YEAR', -30, NOW()), 500, 58.00, 'Chinese', '1st', 'I247.57/C44', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('红高粱', '莫言', '人民文学出版社', '9787020002227', 'LITERATURE', 'NOVEL', 'B区-10架-3列', 1, '当代长篇小说', 'https://example.com/covers/honggaoliang.jpg', 4, 3, 15, DATEADD('YEAR', -30, NOW()), 300, 35.00, 'Chinese', '1st', 'I247.57/M62', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -5, NOW()), NOW(), NOW()),
('蛙', '莫言', '人民文学出版社', '9787020002228', 'LITERATURE', 'NOVEL', 'B区-10架-4列', 1, '当代长篇小说', 'https://example.com/covers/wa.jpg', 3, 2, 12, DATEADD('YEAR', -15, NOW()), 380, 42.00, 'Chinese', '1st', 'I247.57/M62', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -6, NOW()), NOW(), NOW()),
('秦腔', '贾平凹', '人民文学出版社', '9787020002229', 'LITERATURE', 'NOVEL', 'B区-11架-1列', 1, '当代长篇小说', 'https://example.com/covers/qinqiang.jpg', 3, 1, 9, DATEADD('YEAR', -20, NOW()), 500, 55.00, 'Chinese', '1st', 'I247.57/J43', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -7, NOW()), NOW(), NOW()),
('废都', '贾平凹', '人民文学出版社', '9787020002230', 'LITERATURE', 'NOVEL', 'B区-11架-2列', 1, '当代长篇小说', 'https://example.com/covers/feidu.jpg', 3, 2, 10, DATEADD('YEAR', -30, NOW()), 450, 50.00, 'Chinese', '1st', 'I247.57/J43', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -8, NOW()), NOW(), NOW()),
('黄金时代', '王小波', '上海文艺出版社', '9787532128094', 'LITERATURE', 'NOVEL', 'B区-11架-3列', 1, '当代长篇小说', 'https://example.com/covers/huangjinshidai.jpg', 4, 3, 16, DATEADD('YEAR', -25, NOW()), 280, 32.00, 'Chinese', '1st', 'I247.57/W28', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('白银时代', '王小波', '上海文艺出版社', '9787532128095', 'LITERATURE', 'NOVEL', 'B区-11架-4列', 1, '当代长篇小说', 'https://example.com/covers/baiyinshidai.jpg', 3, 2, 13, DATEADD('YEAR', -25, NOW()), 260, 30.00, 'Chinese', '1st', 'I247.57/W28', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('青铜时代', '王小波', '上海文艺出版社', '9787532128096', 'LITERATURE', 'NOVEL', 'B区-12架-1列', 1, '当代长篇小说', 'https://example.com/covers/qingtongshidai.jpg', 3, 1, 10, DATEADD('YEAR', -25, NOW()), 320, 36.00, 'Chinese', '1st', 'I247.57/W28', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('解忧杂货店', '东野圭吾', '南海出版公司', '9787544270878', 'LITERATURE', 'NOVEL', 'B区-12架-2列', 1, '日本长篇小说', 'https://example.com/covers/jieyouzahuodian.jpg', 5, 3, 25, DATEADD('YEAR', -10, NOW()), 291, 39.50, 'Chinese', '1st', 'I313.45/D66', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('白夜行', '东野圭吾', '南海出版公司', '9787544258609', 'LITERATURE', 'NOVEL', 'B区-12架-3列', 1, '日本长篇小说', 'https://example.com/covers/baiyexing.jpg', 5, 2, 22, DATEADD('YEAR', -15, NOW()), 350, 45.00, 'Chinese', '1st', 'I313.45/D66', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -5, NOW()), NOW(), NOW()),
('嫌疑人X的献身', '东野圭吾', '南海出版公司', '9787544246396', 'LITERATURE', 'NOVEL', 'B区-12架-4列', 1, '日本长篇小说', 'https://example.com/covers/xianyiren.jpg', 4, 3, 18, DATEADD('YEAR', -15, NOW()), 251, 35.00, 'Chinese', '1st', 'I313.45/D66', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -6, NOW()), NOW(), NOW()),
('百年孤独', '加西亚·马尔克斯', '南海出版公司', '9787544253996', 'LITERATURE', 'NOVEL', 'B区-13架-1列', 1, '魔幻现实主义文学代表作', 'https://example.com/covers/bainiangudu.jpg', 4, 2, 16, DATEADD('YEAR', -50, NOW()), 360, 39.50, 'Chinese', '1st', 'I775.45/M12', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -7, NOW()), NOW(), NOW()),
('霍乱时期的爱情', '加西亚·马尔克斯', '南海出版公司', '9787544253995', 'LITERATURE', 'NOVEL', 'B区-13架-2列', 1, '魔幻现实主义文学代表作', 'https://example.com/covers/huoluanaiqing.jpg', 3, 2, 13, DATEADD('YEAR', -40, NOW()), 380, 42.00, 'Chinese', '1st', 'I775.45/M12', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -8, NOW()), NOW(), NOW()),
('了不起的盖茨比', 'F·斯科特·菲茨杰拉德', '人民文学出版社', '9787020002231', 'LITERATURE', 'NOVEL', 'B区-13架-3列', 1, '美国长篇小说', 'https://example.com/covers/gaicibi.jpg', 4, 3, 15, DATEADD('YEAR', -90, NOW()), 200, 28.00, 'Chinese', '1st', 'I712.45/F93', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -1, NOW()), NOW(), NOW()),
('麦田里的守望者', 'J·D·塞林格', '人民文学出版社', '9787020002232', 'LITERATURE', 'NOVEL', 'B区-13架-4列', 1, '美国长篇小说', 'https://example.com/covers/maitianli.jpg', 4, 2, 14, DATEADD('YEAR', -70, NOW()), 220, 30.00, 'Chinese', '1st', 'I712.45/S12', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -2, NOW()), NOW(), NOW()),
('1984', '乔治·奥威尔', '北京十月文艺出版社', '9787530210711', 'LITERATURE', 'NOVEL', 'B区-14架-1列', 1, '英国长篇小说', 'https://example.com/covers/1984.jpg', 5, 3, 20, DATEADD('YEAR', -70, NOW()), 320, 38.00, 'Chinese', '1st', 'I561.45/A94', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -3, NOW()), NOW(), NOW()),
('动物农场', '乔治·奥威尔', '北京十月文艺出版社', '9787530210712', 'LITERATURE', 'NOVEL', 'B区-14架-2列', 1, '英国长篇小说', 'https://example.com/covers/dongwunongchang.jpg', 4, 3, 16, DATEADD('YEAR', -70, NOW()), 180, 25.00, 'Chinese', '1st', 'I561.45/A94', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -4, NOW()), NOW(), NOW()),
('小王子', '安托万·德·圣-埃克苏佩里', '人民文学出版社', '9787020002234', 'LITERATURE', 'FICTION', 'B区-14架-3列', 1, '法国童话', 'https://example.com/covers/xiaowangzi.jpg', 5, 4, 25, DATEADD('YEAR', -80, NOW()), 96, 19.00, 'Chinese', '1st', 'I565.88/S12', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -5, NOW()), NOW(), NOW()),
('爱丽丝梦游仙境', '刘易斯·卡罗尔', '人民文学出版社', '9787020002233', 'LITERATURE', 'FICTION', 'B区-14架-4列', 1, '英国童话', 'https://example.com/covers/ailisimenghui.jpg', 4, 3, 18, DATEADD('YEAR', -150, NOW()), 180, 25.00, 'Chinese', '1st', 'I561.88/K18', DATEADD('YEAR', -1, NOW()), DATEADD('MONTH', -6, NOW()), NOW(), NOW());
