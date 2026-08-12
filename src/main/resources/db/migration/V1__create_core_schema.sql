CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE bank_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    opening_balance DECIMAL(19,2) NOT NULL,
    balance DECIMAL(19,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bank_account_number UNIQUE (account_number),
    CONSTRAINT fk_bank_account_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT chk_bank_account_balance CHECK (balance >= 0),
    INDEX idx_account_user (user_id),
    INDEX idx_account_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE auth_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    created_ip VARCHAR(64) NOT NULL,
    user_agent VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_auth_session_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    INDEX idx_auth_session_user (user_id),
    INDEX idx_auth_session_expire (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE transfer_order (
    id VARCHAR(36) NOT NULL,
    payer_account_id BIGINT NOT NULL,
    payee_account_id BIGINT NOT NULL,
    payer_account_number VARCHAR(32) NOT NULL,
    payee_account_number VARCHAR(32) NOT NULL,
    initiated_by_user_id BIGINT NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    remark VARCHAR(140) NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    error_code VARCHAR(64) NULL,
    error_message VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_transfer_payer_idempotency UNIQUE (payer_account_id, idempotency_key),
    CONSTRAINT fk_transfer_payer_account FOREIGN KEY (payer_account_id) REFERENCES bank_account (id),
    CONSTRAINT fk_transfer_payee_account FOREIGN KEY (payee_account_id) REFERENCES bank_account (id),
    CONSTRAINT fk_transfer_initiator FOREIGN KEY (initiated_by_user_id) REFERENCES app_user (id),
    CONSTRAINT chk_transfer_amount CHECK (amount > 0),
    INDEX idx_transfer_initiator_created (initiated_by_user_id, created_at),
    INDEX idx_transfer_payee_created (payee_account_id, created_at),
    INDEX idx_transfer_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ledger_entry (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transfer_id VARCHAR(36) NOT NULL,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(32) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    counterparty_account_number VARCHAR(32) NOT NULL,
    description VARCHAR(140) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ledger_transfer FOREIGN KEY (transfer_id) REFERENCES transfer_order (id),
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES bank_account (id),
    CONSTRAINT chk_ledger_amount CHECK (amount > 0),
    INDEX idx_ledger_account_created (account_id, created_at),
    INDEX idx_ledger_transfer (transfer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE risk_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    account_id BIGINT NULL,
    transfer_id VARCHAR(36) NULL,
    event_type VARCHAR(48) NOT NULL,
    level VARCHAR(16) NOT NULL,
    amount DECIMAL(19,2) NULL,
    reason VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_risk_user_created (user_id, created_at),
    INDEX idx_risk_level_created (level, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128) NULL,
    result VARCHAR(16) NOT NULL,
    request_id VARCHAR(64) NULL,
    client_ip VARCHAR(64) NULL,
    details TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_actor_created (actor_user_id, created_at),
    INDEX idx_audit_action_created (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reconciliation_batch (
    id VARCHAR(36) NOT NULL,
    business_date DATE NOT NULL,
    status VARCHAR(40) NOT NULL,
    total_accounts INT NOT NULL,
    mismatch_count INT NOT NULL,
    initiated_by_user_id BIGINT NULL,
    failure_reason VARCHAR(255) NULL,
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_reconciliation_business_date (business_date, started_at),
    INDEX idx_reconciliation_status (status, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reconciliation_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    batch_id VARCHAR(36) NOT NULL,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(32) NOT NULL,
    persisted_balance DECIMAL(19,2) NOT NULL,
    calculated_balance DECIMAL(19,2) NOT NULL,
    difference DECIMAL(19,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reconciliation_batch_account UNIQUE (batch_id, account_id),
    CONSTRAINT fk_reconciliation_item_batch FOREIGN KEY (batch_id) REFERENCES reconciliation_batch (id),
    INDEX idx_reconciliation_item_status (batch_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
