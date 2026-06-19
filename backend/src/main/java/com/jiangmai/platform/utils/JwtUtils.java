package com.jiangmai.platform.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String DEFAULT_SECRET = "JiangmaiSecretKey2026!";
    private static final long DEFAULT_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24 hours

    private static String getSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = System.getProperty("JWT_SECRET");
        }
        return (secret == null || secret.isBlank()) ? DEFAULT_SECRET : secret;
    }

    private static long getExpireTime() {
        String expiration = System.getenv("JWT_EXPIRE_MS");
        if (expiration == null || expiration.isBlank()) {
            expiration = System.getProperty("JWT_EXPIRE_MS");
        }
        if (expiration == null || expiration.isBlank()) {
            return DEFAULT_EXPIRE_TIME;
        }
        try {
            return Long.parseLong(expiration);
        } catch (NumberFormatException ignored) {
            return DEFAULT_EXPIRE_TIME;
        }
    }

    public static String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + getExpireTime());

        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        return JWT.create()
                .withHeader(header)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(getSecret()));
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(getSecret())).build().verify(token);
    }
}
