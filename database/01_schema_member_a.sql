USE luggage_system;

CREATE TABLE IF NOT EXISTS sys_user
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(30)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname      VARCHAR(30)  NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),

    CONSTRAINT chk_sys_user_role
    CHECK (role IN ('USER', 'ADMIN')),

    CONSTRAINT chk_sys_user_status
    CHECK (status IN ('NORMAL', 'DISABLED'))
    ) ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS locker
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    locker_code VARCHAR(30)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    location    VARCHAR(200) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_locker_code (locker_code),

    CONSTRAINT chk_locker_status
    CHECK (status IN ('ENABLED', 'DISABLED'))
    ) ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS locker_cell
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    locker_id  BIGINT      NOT NULL,
    cell_no    VARCHAR(20) NOT NULL,
    size_type  VARCHAR(20) NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version    INT         NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_locker_cell_no (locker_id, cell_no),

    KEY idx_locker_cell_status_size (status, size_type),

    CONSTRAINT fk_locker_cell_locker
    FOREIGN KEY (locker_id)
    REFERENCES locker (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

    CONSTRAINT chk_locker_cell_size
    CHECK (size_type IN ('SMALL', 'MEDIUM', 'LARGE')),

    CONSTRAINT chk_locker_cell_status
    CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'DISABLED'))
    ) ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;