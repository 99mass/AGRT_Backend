package com.unchk.AGRT_Backend.services;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.unchk.AGRT_Backend.config.JwtProperties;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.repositories.UserRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Autowired
    public AuthenticationService(
            JwtProperties jwtProperties,
            AuthenticationManager authenticationManager,
            UserRepository userRepository) {
        this.jwtProperties = jwtProperties;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    public Map<String, String> authenticate(String email, String password) {
        @SuppressWarnings("unused")
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationTime()))
                .signWith(jwtProperties.getSecretKey())
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole().toString());
        response.put("email", user.getEmail());

        return response;
    }
}