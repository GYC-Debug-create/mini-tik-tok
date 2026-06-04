-- Testcontainers 集成测试用（无 CREATE DATABASE，库名由容器 withDatabaseName 指定）

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT NOW(),
    updated_at  DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    PRIMARY KEY (id),
    CONSTRAINT uk_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS videos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(100) NOT NULL,
    video_url   VARCHAR(500) NOT NULL,
    like_count  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT NOW(),
    updated_at  DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_status_like_created (status, like_count DESC, created_at DESC),
    CONSTRAINT fk_videos_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS likes (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    video_id    BIGINT   NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_video_like UNIQUE (user_id, video_id),
    INDEX idx_video_id (video_id),
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_likes_video FOREIGN KEY (video_id) REFERENCES videos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS visit_records (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    video_id    BIGINT   NOT NULL,
    viewed_at   DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_video_visit UNIQUE (user_id, video_id),
    INDEX idx_user_viewed (user_id, viewed_at DESC),
    CONSTRAINT fk_visit_records_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_visit_records_video FOREIGN KEY (video_id) REFERENCES videos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS request_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       DEFAULT NULL,
    method          VARCHAR(10)  NOT NULL,
    path            VARCHAR(255) NOT NULL,
    request_params  VARCHAR(500) DEFAULT NULL,
    response_code   INT          NOT NULL,
    response_body   VARCHAR(1000) DEFAULT NULL,
    cost_ms         INT          NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO users (id, username, password) VALUES
    (1, 'alice',   '$2b$10$toxUGzhYpcujS4O/eMOFmu/FigoLPDtiKhMEGUL981GHSYQXBokWa'),
    (2, 'bob',     '$2b$10$toxUGzhYpcujS4O/eMOFmu/FigoLPDtiKhMEGUL981GHSYQXBokWa');

INSERT IGNORE INTO videos (id, user_id, title, video_url, like_count, created_at) VALUES
    (1, 1, '搞笑猫咪合集', 'https://example.com/1.mp4', 128, '2026-05-01 10:00:00'),
    (2, 2, '日落延时摄影', 'https://example.com/2.mp4',  95, '2026-05-02 12:00:00'),
    (3, 1, '美食探店',       'https://example.com/3.mp4',  72, '2026-05-03 14:00:00');

INSERT IGNORE INTO visit_records (user_id, video_id, viewed_at) VALUES
    (1, 1, '2026-05-10 10:00:00'),
    (1, 2, '2026-05-10 10:05:00');
