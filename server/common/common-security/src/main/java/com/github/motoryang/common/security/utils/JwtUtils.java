package com.github.motoryang.common.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT utility class for token generation and validation
 */
public final class JwtUtils {

    private JwtUtils() {
    }

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String CLAIM_ROLES = "roles";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * Generate SecretKey from string
     */
    public static SecretKey getSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create access token
     */
    public static String createAccessToken(String userId, String username, List<String> roles,
                                            String secret, long expireMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        claims.put(CLAIM_ROLES, roles);
        return createToken(claims, secret, expireMillis);
    }

    /**
     * Create refresh token
     */
    public static String createRefreshToken(String userId, String username, String secret, long expireMillis) {
        return createToken(Map.of(
                CLAIM_USER_ID, userId,
                CLAIM_USERNAME, username,
                CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH
        ), secret, expireMillis);
    }

    /**
     * Create token with claims
     */
    public static String createToken(Map<String, Object> claims, String secret, long expireMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMillis);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey(secret))
                .compact();
    }

    /**
     * Parse token and get claims
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSecretKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get user ID from token
     */
    public static String getUserId(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get(CLAIM_USER_ID, String.class);
    }

    /**
     * Get username from token
     */
    public static String getUsername(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * Get roles from token
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get(CLAIM_ROLES, List.class);
    }

    /**
     * Get token type from token
     */
    public static String getTokenType(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * Validate if token is valid
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public static boolean isTokenExpired(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get token remaining time in milliseconds
     */
    public static long getTokenRemainingTime(String token, String secret) {
        Claims claims = parseToken(token, secret);
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
