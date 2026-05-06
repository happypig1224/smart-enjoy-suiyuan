package com.shxy.suiyuanserver.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern SCRIPT_ATTR_PATTERN = Pattern.compile(
            "<\\s*script[^>]*>", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ON_EVENT_PATTERN = Pattern.compile(
            "\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
            "<[^>]+>"
    );

    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN = Pattern.compile(
            "(href|src|action)\\s*=\\s*[\"']\\s*javascript\\s*:", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATA_PROTOCOL_PATTERN = Pattern.compile(
            "(src|href)\\s*=\\s*[\"']\\s*data\\s*:", Pattern.CASE_INSENSITIVE
    );

    private byte[] cachedBody;

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return stripXss(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null || values.length == 0) {
            return values;
        }
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = StringUtils.hasText(values[i]) ? stripXss(values[i]) : values[i];
        }
        return sanitized;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBody == null) {
            String contentType = getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(super.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String jsonBody = sb.toString();
                if (StringUtils.hasText(jsonBody)) {
                    jsonBody = sanitizeJsonBody(jsonBody);
                }
                cachedBody = jsonBody.getBytes(StandardCharsets.UTF_8);
            } else {
                cachedBody = super.getInputStream().readAllBytes();
            }
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    private String sanitizeJsonBody(String jsonBody) {
        try {
            Object parsed = JSON.parse(jsonBody);
            Object sanitized = sanitizeJsonValue(parsed);
            return JSON.toJSONString(sanitized);
        } catch (Exception e) {
            return stripXss(jsonBody);
        }
    }

    private Object sanitizeJsonValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String strValue) {
            return stripXss(strValue);
        }
        if (value instanceof JSONObject jsonObj) {
            JSONObject result = new JSONObject();
            for (String key : jsonObj.keySet()) {
                result.put(key, sanitizeJsonValue(jsonObj.get(key)));
            }
            return result;
        }
        if (value instanceof JSONArray jsonArr) {
            JSONArray result = new JSONArray();
            for (int i = 0; i < jsonArr.size(); i++) {
                result.add(sanitizeJsonValue(jsonArr.get(i)));
            }
            return result;
        }
        return value;
    }

    private String stripXss(String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        result = SCRIPT_TAG_PATTERN.matcher(result).replaceAll("");
        result = SCRIPT_ATTR_PATTERN.matcher(result).replaceAll("");
        result = ON_EVENT_PATTERN.matcher(result).replaceAll("");
        result = JAVASCRIPT_PROTOCOL_PATTERN.matcher(result).replaceAll("$1=\"\"");
        result = DATA_PROTOCOL_PATTERN.matcher(result).replaceAll("$1=\"\"");
        return result;
    }
}
