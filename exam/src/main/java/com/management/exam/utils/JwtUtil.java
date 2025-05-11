package com.management.exam.utils;

import com.management.exam.entity.User;
import com.management.exam.entity.Student;
import com.management.exam.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
             byte[] paddedKeyBytes = new byte[32];
             System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, Math.min(keyBytes.length, 32));
             keyBytes = paddedKeyBytes;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        return createToken(claims, user.getUsername());
    }

    public String generateStudentToken(Student student) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", Role.STUDENT.name());
        claims.put("studentId", student.getId());
        return createToken(claims, student.getStudentId());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
         return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Integer extractUserId(String token) {
        // 先尝试获取userId
        Integer userId = extractClaim(token, claims -> claims.get("userId", Integer.class));
        if (userId != null) {
            return userId;
        }

        // 如果userId不存在，尝试获取studentId
        return extractClaim(token, claims -> claims.get("studentId", Integer.class));
    }


    private Boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

     public Boolean validateToken(String token) {
        try {
             extractAllClaims(token);
             return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
} 