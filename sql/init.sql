-- ============================================================
-- 视频流推荐系统（简易版抖音）— 数据库初始化脚本
-- 版本：v2.1（P0-only 开发冻结版）
-- 依据：技术设计文档 v2.1 §4 数据库设计
-- MySQL 要求：8.0+
-- ============================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS douyin_lite
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE douyin_lite;

-- ============================================================
-- 2. 用户表（users）
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL                 COMMENT '用户名',
    password    VARCHAR(255) NOT NULL                 COMMENT 'BCrypt加密密码',
    created_at  DATETIME     NOT NULL DEFAULT NOW()   COMMENT '注册时间',
    updated_at  DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW()
                                                      COMMENT '更新时间',
    PRIMARY KEY (id),
    CONSTRAINT uk_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 3. 视频表（videos）
-- ============================================================
CREATE TABLE IF NOT EXISTS videos (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '视频ID',
    user_id     BIGINT       NOT NULL                 COMMENT '发布者ID',
    title       VARCHAR(100) NOT NULL                 COMMENT '视频标题',
    video_url   VARCHAR(500) NOT NULL                 COMMENT '视频访问URL',
    like_count  INT          NOT NULL DEFAULT 0       COMMENT '点赞数（冗余字段）',
    status      TINYINT      NOT NULL DEFAULT 0       COMMENT '0=正常，1=已删除（软删除）',
    created_at  DATETIME     NOT NULL DEFAULT NOW()   COMMENT '发布时间',
    updated_at  DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW()
                                                      COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_status_like_created (status, like_count DESC, created_at DESC),
    CONSTRAINT fk_videos_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- ============================================================
-- 4. 点赞记录表（likes）
-- ============================================================
CREATE TABLE IF NOT EXISTS likes (
    id          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    user_id     BIGINT   NOT NULL                 COMMENT '点赞用户ID',
    video_id    BIGINT   NOT NULL                 COMMENT '被点赞视频ID',
    created_at  DATETIME NOT NULL DEFAULT NOW()   COMMENT '点赞时间',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_video_like UNIQUE (user_id, video_id),
    INDEX idx_video_id (video_id),
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_likes_video FOREIGN KEY (video_id) REFERENCES videos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- ============================================================
-- 5. 访问记录表（visit_records）
-- ============================================================
CREATE TABLE IF NOT EXISTS visit_records (
    id          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    user_id     BIGINT   NOT NULL                 COMMENT '用户ID',
    video_id    BIGINT   NOT NULL                 COMMENT '视频ID',
    viewed_at   DATETIME NOT NULL DEFAULT NOW()   COMMENT '访问时间',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_video_visit UNIQUE (user_id, video_id),
    INDEX idx_user_viewed (user_id, viewed_at DESC),
    INDEX idx_visit_video_id (video_id),
    CONSTRAINT fk_visit_records_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_visit_records_video FOREIGN KEY (video_id) REFERENCES videos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问记录表';

-- ============================================================
-- 6. 请求日志表（request_logs）
-- ============================================================
CREATE TABLE IF NOT EXISTS request_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    user_id         BIGINT       DEFAULT NULL             COMMENT '用户ID（未登录时为NULL）',
    method          VARCHAR(10)  NOT NULL                 COMMENT 'HTTP方法',
    path            VARCHAR(255) NOT NULL                 COMMENT '请求路径',
    request_params  VARCHAR(500) DEFAULT NULL             COMMENT '请求参数摘要（截断500字符）',
    response_code   INT          NOT NULL                 COMMENT 'HTTP响应状态码',
    response_body   VARCHAR(1000) DEFAULT NULL            COMMENT '响应体摘要（截断1000字符，脱敏）',
    cost_ms         INT          NOT NULL                 COMMENT '接口耗时（毫秒）',
    created_at      DATETIME     NOT NULL DEFAULT NOW()   COMMENT '请求时间',
    PRIMARY KEY (id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';

-- ============================================================
-- 7. 示例数据（用于开发测试和答辩演示）
-- ============================================================

-- 7.1 测试用户（密码明文均为 "123456"，hash 为有效 BCrypt，可真实登录）
INSERT IGNORE INTO users (id, username, password) VALUES
    (1, 'alice',   '$2b$10$toxUGzhYpcujS4O/eMOFmu/FigoLPDtiKhMEGUL981GHSYQXBokWa'),
    (2, 'bob',     '$2b$10$toxUGzhYpcujS4O/eMOFmu/FigoLPDtiKhMEGUL981GHSYQXBokWa'),
    (3, 'charlie', '$2b$10$toxUGzhYpcujS4O/eMOFmu/FigoLPDtiKhMEGUL981GHSYQXBokWa');

-- 7.2 测试视频（8条，不同发布时间和点赞数，验证推荐排序和分页）
INSERT IGNORE INTO videos (id, user_id, title, video_url, like_count, created_at) VALUES
    (1, 1, '搞笑猫咪合集',       'https://www.w3schools.com/html/mov_bbb.mp4',  128, '2026-05-01 10:00:00'),
    (2, 2, '日落延时摄影',       'https://www.w3schools.com/html/mov_bbb.mp4',   95, '2026-05-02 12:00:00'),
    (3, 3, '街舞教学第一课',     'https://www.w3schools.com/html/mov_bbb.mp4',   72, '2026-05-03 14:00:00'),
    (4, 1, '美食探店：深夜烧烤', 'https://www.w3schools.com/html/mov_bbb.mp4',   55, '2026-05-04 16:00:00'),
    (5, 2, '编程入门：Hello World','https://www.w3schools.com/html/mov_bbb.mp4', 40, '2026-05-05 18:00:00'),
    (6, 3, '旅行Vlog：云南大理',  'https://www.w3schools.com/html/mov_bbb.mp4',   30, '2026-05-06 20:00:00'),
    (7, 1, '吉他弹唱：晴天',     'https://www.w3schools.com/html/mov_bbb.mp4',   25, '2026-05-07 09:00:00'),
    (8, 2, '健身打卡第30天',     'https://www.w3schools.com/html/mov_bbb.mp4',   15, '2026-05-08 11:00:00');

-- 7.3 测试点赞（3个用户交叉点赞）
--     注：videos.like_count 与 likes 表记录数一致是生产代码的事务职责；
--     示例数据的 like_count 为预设值，与下方 INSERT 条数可能不一致，仅用于演示排序。
INSERT IGNORE INTO likes (user_id, video_id) VALUES
    (1, 2), (1, 3), (1, 5),   -- alice 点赞日落延时、街舞教学、编程入门
    (2, 1), (2, 3),            -- bob 点赞搞笑猫咪、街舞教学
    (3, 1), (3, 2);            -- charlie 点赞搞笑猫咪、日落延时

-- 7.4 测试访问记录（alice 已看过 id=1 和 id=2，推荐给 alice 时应过滤）
INSERT IGNORE INTO visit_records (user_id, video_id, viewed_at) VALUES
    (1, 1, '2026-05-10 10:00:00'),   -- 最早
    (1, 2, '2026-05-10 10:05:00');   -- 最近 → 当前视频

-- 答辩/联调：request_logs 查询示例见同目录 monitoring-queries.sql
