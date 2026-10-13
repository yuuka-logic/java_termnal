package com.example.personal_blog.common;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取请求体的包装器。
 * <p>
 * Servlet 的请求体是流，读一次就没了。操作日志拦截器需要读取请求体做记录，
 * 若直接读会导致后面的 @RequestBody 反序列化拿到空流。
 * 这里把请求体先缓存成 byte[]，之后无论读多少次都从缓存返回。
 */
public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public RepeatableReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = readAll(request);
    }

    private static byte[] readAll(HttpServletRequest request) throws IOException {
        ServletInputStream in = request.getInputStream();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 同步读取，无需实现
            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
