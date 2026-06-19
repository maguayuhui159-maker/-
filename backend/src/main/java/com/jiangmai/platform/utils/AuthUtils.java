package com.jiangmai.platform.utils;

import com.auth0.jwt.interfaces.DecodedJWT;

public class AuthUtils {

    private AuthUtils() {}

    public static String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.trim();
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        return token.isBlank() ? null : token;
    }

    public static Long parseUserId(String authorization) {
        try {
            String token = extractToken(authorization);
            if (token == null) return null;
            DecodedJWT jwt = JwtUtils.verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public static String parseRole(String authorization) {
        try {
            String token = extractToken(authorization);
            if (token == null) return null;
            DecodedJWT jwt = JwtUtils.verify(token);
            return jwt.getClaim("role").asString();
        } catch (Exception e) {
            return null;
        }
    }
}
