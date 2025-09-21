package com.example.datasetapi.util;

import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.Role;
import com.example.datasetapi.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private final Key key;  // Dùng để ký và verify token
    private final long expirationTime;

    // Spring sẽ inject từ application.properties
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationTime) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // String -> Key
        this.expirationTime = expirationTime;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName)   // ví dụ ["USER", "ADMIN"]
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 phút
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("user_Id",userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 ngày
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    // Lấy username từ token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Kiểm tra token còn hợp lệ hay không
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername) && !isTokenExpired(token));
    }

    // ================== PRIVATE METHODS ==================

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // ✅ dùng key để verify
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

}