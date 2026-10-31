package com.example.student.security;

import com.example.student.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final SecretKey secretKey;

    public JwtService(@Value("${app.security.jwt-secret}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET must be configured");
        }
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
    }
    public String generateToken(String username, Set<Role> roles) {
        List<String> roleNames = roles.stream()
                .map(Enum::name)
                .toList();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roleNames)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 час
                .signWith(secretKey)
                .compact();
    }
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    @SuppressWarnings("unchecked")
    public Set<Role> extractRoles(String token) {
        List<String> roleNames = parseClaims(token).get("roles", List.class);
        return roleNames.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}