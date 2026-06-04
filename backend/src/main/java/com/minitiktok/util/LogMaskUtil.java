package com.minitiktok.util;

import java.util.regex.Pattern;

public final class LogMaskUtil {

    private static final int MAX_REQUEST_LEN = 500;
    private static final int MAX_RESPONSE_LEN = 1000;

    private static final Pattern PASSWORD_FIELD =
            Pattern.compile("(\"password\"\\s*:\\s*)\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_FIELD =
            Pattern.compile("(\"token\"\\s*:\\s*)\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTHORIZATION_BEARER =
            Pattern.compile("(authorization\\s*:\\s*Bearer\\s+)[^\\s,}]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTHORIZATION_HEADER =
            Pattern.compile("(authorization\\s*[:=]\\s*)[^,\\s}]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern BEARER_TOKEN =
            Pattern.compile("(Bearer\\s+)[A-Za-z0-9._\\-]+", Pattern.CASE_INSENSITIVE);

    private LogMaskUtil() {
    }

    public static String mask(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String masked = PASSWORD_FIELD.matcher(raw).replaceAll("$1\"***\"");
        masked = TOKEN_FIELD.matcher(masked).replaceAll("$1\"***\"");
        masked = AUTHORIZATION_BEARER.matcher(masked).replaceAll("$1***");
        masked = AUTHORIZATION_HEADER.matcher(masked).replaceAll("$1***");
        masked = BEARER_TOKEN.matcher(masked).replaceAll("$1***");
        return masked;
    }

    public static String truncateRequest(String raw) {
        return truncate(raw, MAX_REQUEST_LEN);
    }

    public static String truncateResponse(String raw) {
        return truncate(mask(raw), MAX_RESPONSE_LEN);
    }

    private static String truncate(String raw, int max) {
        if (raw == null) {
            return null;
        }
        return raw.length() <= max ? raw : raw.substring(0, max);
    }
}
