-- ============================================================
-- 用户管理接口测试数据
-- 所有测试账号的密码统一为 123456，密文由 PasswordUtil.encrypt 生成
-- （格式：迭代次数$盐(Base64)$摘要(Base64)，可直接被 PasswordUtil.matches 校验通过）
-- ============================================================
USE `personal_blog`;

-- 注意：user 被 article/announcement/operation_log 的外键引用，TRUNCATE 会报 ERROR 1701，
--       所以这里用 DELETE 清空数据，再重置自增计数器。
DELETE FROM `user`;
ALTER TABLE `user` AUTO_INCREMENT = 1;

INSERT INTO `user`
    (username, password, nickname, email, phone, gender, bio, role, status, is_deleted)
VALUES
('admin',    '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '超级管理员', 'admin@blog.com',   '13800000001', 1, '系统最高权限账号',     0, 1, 0),
('manager',  '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '内容管理员', 'manager@blog.com', '13800000002', 2, '负责文章审核',         1, 1, 0),
('zhangsan', '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '张三',       'zhang@blog.com',   '13800000003', 1, '爱写 Java 的大三学生', 2, 1, 0),
('lisi',     '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '李四',       'li@blog.com',      '13800000004', 1, '前端爱好者',           2, 1, 0),
('wangwu',   '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '王五',       'wang@blog.com',    '13800000005', 2, '数据库方向',           2, 1, 0),
('zhaoliu',  '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '赵六',       'zhao@blog.com',    '13800000006', 0, NULL,                   2, 0, 0),
('sunqi',    '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '孙七',       'sun@blog.com',     NULL,          2, '已被逻辑删除的账号',   2, 0, 1),
('zhouba',   '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=', '周八',       NULL,               NULL,          1, NULL,                   2, 1, 0);
