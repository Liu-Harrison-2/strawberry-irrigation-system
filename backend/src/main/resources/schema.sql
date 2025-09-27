-- PostgreSQL用户管理模块数据库表结构

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    real_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    user_type VARCHAR(20) NOT NULL DEFAULT 'FARMER',
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    remark TEXT,

    -- 约束检查
    CONSTRAINT chk_user_type CHECK (user_type IN ('ADMIN', 'FARMER')),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED')),
    CONSTRAINT chk_phone_format CHECK (phone_number ~ '^1[3-9]\d{9}$')
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- 创建触发器函数用于自动更新updated_at字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建触发器
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加表和字段注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID';
COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password_hash IS '密码哈希';
COMMENT ON COLUMN users.email IS '邮箱';
COMMENT ON COLUMN users.real_name IS '真实姓名';
COMMENT ON COLUMN users.phone_number IS '手机号';
COMMENT ON COLUMN users.user_type IS '用户类型';
COMMENT ON COLUMN users.status IS '用户状态';
COMMENT ON COLUMN users.is_verified IS '是否验证';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';
COMMENT ON COLUMN users.remark IS '备注';

-- ========================================
-- 认证授权模块数据库表结构
-- ========================================

-- 创建刷新令牌表
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    device_info VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,
    revoked_reason VARCHAR(100),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_is_revoked ON refresh_tokens(is_revoked);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_created_at ON refresh_tokens(created_at);

-- 创建触发器用于自动更新refresh_tokens表的updated_at字段
DROP TRIGGER IF EXISTS update_refresh_tokens_updated_at ON refresh_tokens;
CREATE TRIGGER update_refresh_tokens_updated_at
    BEFORE UPDATE ON refresh_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加表和字段注释
COMMENT ON TABLE refresh_tokens IS '刷新令牌表';
COMMENT ON COLUMN refresh_tokens.id IS '令牌ID';
COMMENT ON COLUMN refresh_tokens.token_hash IS '令牌哈希值（SHA-256）';
COMMENT ON COLUMN refresh_tokens.user_id IS '关联用户ID';
COMMENT ON COLUMN refresh_tokens.expires_at IS '令牌过期时间';
COMMENT ON COLUMN refresh_tokens.is_revoked IS '是否已撤销';
COMMENT ON COLUMN refresh_tokens.device_info IS '设备信息';
COMMENT ON COLUMN refresh_tokens.ip_address IS 'IP地址';
COMMENT ON COLUMN refresh_tokens.user_agent IS '用户代理信息';
COMMENT ON COLUMN refresh_tokens.created_at IS '创建时间';
COMMENT ON COLUMN refresh_tokens.updated_at IS '更新时间';
COMMENT ON COLUMN refresh_tokens.revoked_at IS '撤销时间';
COMMENT ON COLUMN refresh_tokens.revoked_reason IS '撤销原因';

-- 创建用于清理过期令牌的函数
CREATE OR REPLACE FUNCTION cleanup_expired_refresh_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM refresh_tokens 
    WHERE expires_at < CURRENT_TIMESTAMP 
       OR (is_revoked = TRUE AND revoked_at < CURRENT_TIMESTAMP - INTERVAL '7 days');
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    -- 记录清理日志
    INSERT INTO system_logs (level, message, module, created_at)
    VALUES ('INFO', 
            'Cleaned up ' || deleted_count || ' expired/revoked refresh tokens',
            'AUTH_CLEANUP',
            CURRENT_TIMESTAMP);
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- 创建系统日志表（如果不存在）
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(10) NOT NULL,
    message TEXT NOT NULL,
    module VARCHAR(50),
    user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_system_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 创建系统日志表的索引
CREATE INDEX IF NOT EXISTS idx_system_logs_level ON system_logs(level);
CREATE INDEX IF NOT EXISTS idx_system_logs_module ON system_logs(module);
CREATE INDEX IF NOT EXISTS idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);

-- 添加系统日志表注释
COMMENT ON TABLE system_logs IS '系统日志表';
COMMENT ON COLUMN system_logs.id IS '日志ID';
COMMENT ON COLUMN system_logs.level IS '日志级别（INFO, WARN, ERROR）';
COMMENT ON COLUMN system_logs.message IS '日志内容';
COMMENT ON COLUMN system_logs.module IS '产生日志的模块名';
COMMENT ON COLUMN system_logs.user_id IS '关联的用户ID（可选）';
COMMENT ON COLUMN system_logs.created_at IS '日志时间戳';