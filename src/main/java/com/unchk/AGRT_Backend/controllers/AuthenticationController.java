package com.unchk.AGRT_Backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unchk.AGRT_Backend.services.AuthenticationService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, String> token = authenticationService.authenticate(
                    loginRequest.getEmail(),
                    loginRequest.getPassword());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "Email ou mot de passe incorrect");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "Une erreur est survenue lors de l'authentification");
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

@Data
class LoginRequest {
    private String email;
    private String password;
}