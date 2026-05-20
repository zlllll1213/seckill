CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seckill;

CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `username`   VARCHAR(64)  NOT NULL UNIQUE,
    `password`   VARCHAR(255) NOT NULL,
    `email`      VARCHAR(128),
    `role`       VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(128)  NOT NULL,
    `description` TEXT,
    `price`       DECIMAL(10,2) NOT NULL,
    `image_url`   VARCHAR(512),
    `stock`       INT           NOT NULL DEFAULT 0,
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=上架 0=下架',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `seckill_activity` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `product_id`    BIGINT        NOT NULL,
    `name`          VARCHAR(128)  NOT NULL,
    `seckill_price` DECIMAL(10,2) NOT NULL,
    `stock`         INT           NOT NULL DEFAULT 0,
    `start_time`    DATETIME      NOT NULL,
    `end_time`      DATETIME      NOT NULL,
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '0=待开始 1=进行中 2=已结束',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `seckill_order` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT        NOT NULL,
    `activity_id` BIGINT        NOT NULL,
    `product_id`  BIGINT        NOT NULL,
    `price`       DECIMAL(10,2) NOT NULL,
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0=待支付 1=已支付 2=已取消',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始测试账号（密码均为: admin123，BCrypt 加密）
INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES
('admin', '$2a$10$ygko5GQiNRIk1Td5ZBex6u9gEjzpfDQnkU3cYcsMxnWz7/rS.QZ7u', 'admin@flashforge.local', 'ADMIN'),
('alice', '$2a$10$ygko5GQiNRIk1Td5ZBex6u9gEjzpfDQnkU3cYcsMxnWz7/rS.QZ7u', 'alice@flashforge.local', 'USER'),
('bob', '$2a$10$ygko5GQiNRIk1Td5ZBex6u9gEjzpfDQnkU3cYcsMxnWz7/rS.QZ7u', 'bob@flashforge.local', 'USER'),
('tester', '$2a$10$ygko5GQiNRIk1Td5ZBex6u9gEjzpfDQnkU3cYcsMxnWz7/rS.QZ7u', 'tester@flashforge.local', 'USER')
ON DUPLICATE KEY UPDATE
    `email` = VALUES(`email`),
    `role` = VALUES(`role`);

-- 初始秒杀商品
INSERT INTO `product` (`name`, `description`, `price`, `image_url`, `stock`, `status`)
SELECT '闪购机械键盘', '热插拔轴体，RGB 背光，适合办公与游戏的限时秒杀款。', 199.00, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80', 120, 1
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '闪购机械键盘');

INSERT INTO `product` (`name`, `description`, `price`, `image_url`, `stock`, `status`)
SELECT '降噪蓝牙耳机', '主动降噪，长续航，通勤与运动都能稳定使用。', 299.00, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80', 88, 1
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '降噪蓝牙耳机');

INSERT INTO `product` (`name`, `description`, `price`, `image_url`, `stock`, `status`)
SELECT '智能手表 Pro', '健康监测、消息提醒和运动记录，秒杀专享价。', 399.00, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80', 60, 1
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '智能手表 Pro');

INSERT INTO `product` (`name`, `description`, `price`, `image_url`, `stock`, `status`)
SELECT '高速移动固态硬盘 1TB', '高速读写，小巧便携，适合备份、剪辑和随身资料库。', 459.00, 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=900&q=80', 45, 1
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '高速移动固态硬盘 1TB');
