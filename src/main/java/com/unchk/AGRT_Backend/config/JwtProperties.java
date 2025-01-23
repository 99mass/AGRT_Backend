package com.unchk.AGRT_Backend.config;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;


@Component
public class JwtProperties {
    @Value("${jwt.secret}")
    private String secretKeyString;
    
    private SecretKey secretKey;
    private final long expirationTime = 86400000; // 24 heures

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKeyString.getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}