package com.example.personal_blog.common;

/**
 * 当前登录用户上下文。
 * 基于 ThreadLocal，只在单次请求线程内有效，登录成功后由 AuthService 写入，
 * 由 LoginContextInterceptor 在请求结束时清理，避免线程复用导致串号。
 */
public final class UserContext {

    /** 当前登录用户，未登录时为 null */
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.getId();
    }

    public static String getUsername() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.getUsername();
    }

    public static Integer getRole() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.getRole();
    }

    public static boolean isLogin() {
        return HOLDER.get() != null;
    }

    /** 必须在请求结束时调用，否则线程池复用会造成用户串号 */
    public static void clear() {
        HOLDER.remove();
    }

    /** 登录用户快照 */
    public static class CurrentUser {
        private final Long id;
        private final String username;
        private final String nickname;
        private final Integer role;

        public CurrentUser(Long id, String username, String nickname, Integer role) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.role = role;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getNickname() { return nickname; }
        public Integer getRole() { return role; }
    }
}
