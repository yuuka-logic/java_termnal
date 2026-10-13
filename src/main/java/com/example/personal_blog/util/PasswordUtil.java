package com.example.personal_blog.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码加密工具：PBKDF2 风格的「随机盐 + 迭代 SHA-256」，只用 JDK 自带 API，无需额外依赖。
 * <p>
 * 存储格式：{@code 迭代次数$盐(Base64)$摘要(Base64)}，例如 {@code 10000$yQ8k...$9f3a...}
 * <p>
 * 说明：生产环境更常用 BCrypt（Spring Security 的 BCryptPasswordEncoder）。
 * 这里不引入 spring-security-crypto，是为了让项目零新增依赖即可运行；
 * 如果后续想换 BCrypt，只需替换本类的 encrypt / matches 两个方法，调用方不用改。
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 10000;
    private static final int SALT_BYTES = 16;
    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * 加密明文密码。
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] digest = digest(rawPassword, salt, ITERATIONS);
        return ITERATIONS + "$" + Base64.encode(salt) + "$" + Base64.encode(digest);
    }

    /**
     * 校验明文密码与密文是否匹配。
     */
    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.decode(parts[1]);
            byte[] expected = Base64.decode(parts[2]);
            byte[] actual = digest(rawPassword, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] digest(String rawPassword, byte[] salt, int iterations) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] result = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            for (int i = 1; i < iterations; i++) {
                md.reset();
                result = md.digest(result);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 " + ALGORITHM, e);
        }
    }

    /** 极简 Base64 封装，避免依赖 java.util.Base64 之外的第三方库 */
    private static final class Base64 {
        static String encode(byte[] data) {
            return java.util.Base64.getEncoder().encodeToString(data);
        }

        static byte[] decode(String text) {
            return java.util.Base64.getDecoder().decode(text);
        }
    }
}
