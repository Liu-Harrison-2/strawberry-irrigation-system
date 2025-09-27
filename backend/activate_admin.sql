-- 激活admin003管理员账户
UPDATE users SET status = 'ACTIVE' WHERE username = 'admin003';

-- 验证更新结果
SELECT id, username, real_name, user_type, status FROM users WHERE username = 'admin003';