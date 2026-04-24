package org.example.projektarendehantering.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the originating client IP for proxied requests.
 */
final class ClientIpResolver {

    private ClientIpResolver() {
    }

    static String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        // Trust forwarded headers only when the direct peer is an internal/trusted proxy.
        if (isTrustedProxy(remoteAddr) && forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            for (String part : parts) {
                String candidate = part.trim();
                String normalized = normalizeIpToken(candidate);
                if (normalized != null) {
                    return normalized;
                }
            }
        }
        return remoteAddr;
    }

    private static boolean isTrustedProxy(String remoteAddr) {
        String normalizedRemote = normalizeIpToken(remoteAddr);
        if (normalizedRemote == null) {
            return false;
        }
        return isLoopback(normalizedRemote) || isPrivateIpv4(normalizedRemote);
    }

    private static String normalizeIpToken(String value) {
        if (value == null) {
            return null;
        }
        String candidate = value.trim();
        if (candidate.isEmpty() || "unknown".equalsIgnoreCase(candidate)) {
            return null;
        }
        if (candidate.startsWith("[") && candidate.endsWith("]")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        int colonCount = countOccurrences(candidate, ':');
        if (colonCount == 1 && candidate.contains(".")) {
            candidate = candidate.substring(0, candidate.lastIndexOf(':'));
        }
        if (isIpv4Literal(candidate)) {
            return candidate;
        }
        if (isIpv6Literal(candidate)) {
            return candidate.toLowerCase();
        }
        return null;
    }

    private static int countOccurrences(String value, char needle) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private static boolean isLoopback(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    private static boolean isPrivateIpv4(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            return false;
        }
        try {
            int first = Integer.parseInt(octets[0]);
            int second = Integer.parseInt(octets[1]);
            if (first == 10) {
                return true;
            }
            if (first == 172) {
                return second >= 16 && second <= 31;
            }
            return first == 192 && second == 168;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean isIpv4Literal(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3) {
                return false;
            }
            for (int i = 0; i < octet.length(); i++) {
                if (!Character.isDigit(octet.charAt(i))) {
                    return false;
                }
            }
            int value = Integer.parseInt(octet);
            if (value < 0 || value > 255) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIpv6Literal(String ip) {
        if (!ip.contains(":") || ip.contains(".")) {
            return false;
        }
        for (int i = 0; i < ip.length(); i++) {
            char c = ip.charAt(i);
            boolean isHex = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f')
                    || (c >= 'A' && c <= 'F');
            if (!(isHex || c == ':')) {
                return false;
            }
        }
        return true;
    }
}
