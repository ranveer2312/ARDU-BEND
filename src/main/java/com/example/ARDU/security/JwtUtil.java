package com.example.ARDU.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // secret and expiration are injected from application.properties for easier config
    @Value("${jwt.secret:replace_this_with_a_very_long_and_random_secret_key_which_is_secure!}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}") // default 24h
    private long expirationMs;

    private Key secretKey;

    @PostConstruct
    public void init() {
        // ensure key is of sufficient length
        byte[] keyBytes = secret.getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // public extractor for email (subject)
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // public extractor for role claim
    public String extractRole(String token) {
        Object r = parseClaims(token).get("role");
        return r != null ? r.toString() : null;
    }

    // public check for expiration
    public boolean isTokenExpired(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.before(new Date());
    }

    public boolean validateToken(String token, String expectedEmail) {
        try {
            String email = extractEmail(token);
            return email != null && email.equalsIgnoreCase(expectedEmail) && !isTokenExpired(token);
        } catch (JwtException ex) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
