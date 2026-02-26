package com.yaprj.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String secretKeyString;
    
    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    @Value("${app.jwt.admin-token-expiration}")
    private long adminTokenExpiration;
    
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }
    
    public String createAccessToken(String userId, String email) {
        return createToken(userId, email, "user", accessTokenExpiration);
    }
    
    public String createRefreshToken(String userId) {
        return createToken(userId, null, "refresh", refreshTokenExpiration);
    }
    
    public String createAdminToken(String adminId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + adminTokenExpiration);
        
        return Jwts.builder()
                .subject(adminId)
                .claim("username", username)
                .claim("type", "admin")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
    
    private String createToken(String userId, String email, String type, long expiration) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        
        JwtBuilder builder = Jwts.builder()
                .subject(userId)
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey);
        
        if (email != null) {
            builder.claim("email", email);
        }
        
        return builder.compact();
    }
    
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
    
    public String getUserId(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    public boolean isAdminToken(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims != null && "admin".equals(claims.get("type"));
    }
    
    public boolean validateToken(String token) {
        return validateAndGetClaims(token) != null;
    }
}
