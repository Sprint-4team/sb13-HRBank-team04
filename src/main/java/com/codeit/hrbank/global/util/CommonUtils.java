package com.codeit.hrbank.global.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtils {

    private static final String UNKNOWN = "unknown";

    public static String getRemoteIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (!hasText(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (!hasText(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (!hasText(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (!hasText(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }

        if (!hasText(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (!hasText(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 프록시를 여러 번 거치면 IP가 쉼표로 구분되므로 최초 클라이언트 IP 사용
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        // 로컬 개발 환경의 IPv6 루프백 주소 변환
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)
                || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        return ipAddress;
    }

    private static boolean hasText(String value) {
        return value != null
                && !value.isBlank()
                && !UNKNOWN.equalsIgnoreCase(value);
    }
}