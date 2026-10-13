package com.example.personal_blog.util;

/**
 * 字符串工具：避免为几个简单判断引入 commons-lang3 依赖。
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /** 去空白；空白串返回 null，便于「空值即不修改」的动态 SQL 判断 */
    public static String trimToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    /** 去空白；空白串返回 null，否则截断到指定长度，防止超出数据库列宽报错 */
    public static String trimToNull(String s, int maxLength) {
        String t = trimToNull(s);
        if (t != null && t.length() > maxLength) {
            t = t.substring(0, maxLength);
        }
        return t;
    }

    /**
     * 生成 URL 别名：非字母数字汉字统一转成连字符。
     * 例："Java 基础 / 入门" -> "java-基础-入门"
     */
    public static String toSlug(String name) {
        if (isBlank(name)) {
            return null;
        }
        String s = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-+|-+$", "");
        return s.isEmpty() ? null : s;
    }
}
