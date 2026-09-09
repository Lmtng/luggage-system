USE luggage_system;

CREATE TABLE IF NOT EXISTS storage_order (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
                                             order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
                                             user_id BIGINT NOT NULL COMMENT '用户ID',
                                             cell_id BIGINT NOT NULL COMMENT '柜格ID',
                                             pickup_code_hash VARCHAR(100) NOT NULL COMMENT '取件码摘要',
                                             start_time DATETIME NOT NULL COMMENT '寄存开始时间',
                                             end_time DATETIME DEFAULT NULL COMMENT '实际结束时间',
                                             amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最终费用',
                                             payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID',
                                             status VARCHAR(20) NOT NULL DEFAULT 'STORED' COMMENT 'STORED/PENDING_PAYMENT/COMPLETED/CANCELLED/EXCEPTION',
                                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             UNIQUE KEY uk_order_no (order_no),
                                             KEY idx_user_id (user_id),
                                             KEY idx_cell_id (cell_id),
                                             KEY idx_status (status),
                                             KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寄存订单表';

CREATE TABLE IF NOT EXISTS price_rule (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
                                          size_type VARCHAR(10) NOT NULL COMMENT 'SMALL/MEDIUM/LARGE',
                                          unit_minutes INT NOT NULL DEFAULT 60 COMMENT '一个计费单位的分钟数',
                                          unit_price DECIMAL(10,2) NOT NULL COMMENT '每单位价格',
                                          free_minutes INT NOT NULL DEFAULT 0 COMMENT '免费分钟数',
                                          cap_amount DECIMAL(10,2) DEFAULT NULL COMMENT '单次封顶金额',
                                          enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
                                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          UNIQUE KEY uk_size_type_enabled (size_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='柜格规格计费规则表';

CREATE TABLE IF NOT EXISTS operation_log (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
                                             operator_id BIGINT NOT NULL COMMENT '操作人ID',
                                             operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
                                             target_type VARCHAR(30) NOT NULL COMMENT '目标类型',
                                             target_id BIGINT DEFAULT NULL COMMENT '目标记录ID',
                                             detail VARCHAR(500) DEFAULT NULL COMMENT '操作说明',
                                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                             KEY idx_operator_id (operator_id),
                                             KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';



-- ============================================================
-- 管理员账号：admin;admin123
-- ============================================================
INSERT INTO sys_user (username, password_hash, nickname, role, status) VALUES
    ('admin', '$2a$10$a6kO/ycllcsRUsSp62RXXOzRhIEPbTPcmnjfNOhV9eY67BjPjUnDC', '系统管理员', 'ADMIN', 'NORMAL');

INSERT INTO price_rule (size_type, unit_minutes, unit_price, free_minutes, cap_amount, enabled) VALUES
                                                                                                    ('SMALL', 60, 2.00, 30, 20.00, 1),
                                                                                                    ('MEDIUM', 60, 4.00, 30, 35.00, 1),
                                                                                                    ('LARGE', 60, 6.00, 30, 50.00, 1);

INSERT INTO locker (locker_code, name, location, status) VALUES
    ('LOCKER001', 'A区寄存柜', '南京站A区入口处', 'ENABLED');

INSERT INTO locker_cell (locker_id, cell_no, size_type, status, version) VALUES
                                                                             (1, 'A-01', 'SMALL', 'AVAILABLE', 0),
                                                                             (1, 'A-02', 'SMALL', 'AVAILABLE', 0),
                                                                             (1, 'A-03', 'MEDIUM', 'AVAILABLE', 0),
                                                                             (1, 'A-04', 'MEDIUM', 'AVAILABLE', 0),
                                                                             (1, 'A-05', 'LARGE', 'AVAILABLE', 0),
                                                                             (1, 'A-06', 'LARGE', 'AVAILABLE', 0);