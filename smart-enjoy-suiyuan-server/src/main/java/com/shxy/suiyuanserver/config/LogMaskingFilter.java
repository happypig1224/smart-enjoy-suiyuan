package com.shxy.suiyuanserver.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.regex.Pattern;

public class LogMaskingFilter extends TurboFilter {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(password|passwd|pwd)['\"]?\\s*[:=]\\s*['\"]?[^'\"\\s,}]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(token|secret|key|apikey|api_key)['\"]?\\s*[:=]\\s*['\"]?[^'\"\\s,}]{8,}", Pattern.CASE_INSENSITIVE);

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (format == null) return FilterReply.NEUTRAL;

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String) {
                    String param = (String) params[i];
                    String maskedParam = maskSensitiveInfo(param);
                    if (!maskedParam.equals(param)) {
                        params[i] = maskedParam;
                    }
                }
            }
        }

        return FilterReply.NEUTRAL;
    }

    private String maskSensitiveInfo(String message) {
        if (message == null) return null;
        message = PHONE_PATTERN.matcher(message).replaceAll("$1****$2");
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1=******");
        message = TOKEN_PATTERN.matcher(message).replaceAll("$1=****");
        return message;
    }
}
