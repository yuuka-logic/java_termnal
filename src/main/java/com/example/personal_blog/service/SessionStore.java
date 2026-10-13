package com.example.personal_blog.service;

import com.example.personal_blog.common.UserContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存会话管理。
 * <p>
 * 实训项目够用：登录成功后签发 token，缓存在 ConcurrentHashMap 里，带过期时间。
 * 生产环境应换成 JWT 或 Redis 共享会话（多实例部署时内存会话不通用）。
 */
@Component
public class SessionStore {

    /** token 有效期：2 小时 */
    private static final long TTL_MILLIS = 2 * 60 * 60 * 1000L;

    /** 最多保留的会话数，超出时清理过期项，防止内存无限增长 */
    private static final int MAX_SESSIONS = 10000;

    private static class Session {
        final UserContext.CurrentUser user;
        final long expireAt;

        Session(UserContext.CurrentUser user, long expireAt) {
            this.user = user;
            this.expireAt = expireAt;
        }

        boolean expired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /** 登录成功，签发 token */
    public String create(UserContext.CurrentUser user) {
        if (sessions.size() > MAX_SESSIONS) {
            cleanExpired();
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(user, System.currentTimeMillis() + TTL_MILLIS));
        return token;
    }

    /** 取会话；已过期则顺手删除并返回 null */
    public UserContext.CurrentUser get(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        Session s = sessions.get(token);
        if (s == null) {
            return null;
        }
        if (s.expired()) {
            sessions.remove(token);
            return null;
        }
        return s.user;
    }

    /** 退出登录 */
    public boolean remove(String token) {
        return token != null && sessions.remove(token) != null;
    }

    public int size() {
        return sessions.size();
    }

    private void cleanExpired() {
        sessions.entrySet().removeIf(e -> e.getValue().expired());
    }
}
