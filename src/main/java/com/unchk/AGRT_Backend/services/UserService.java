package com.unchk.AGRT_Backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unchk.AGRT_Backend.config.JwtProperties;
import com.unchk.AGRT_Backend.dto.UserDTO;
import com.unchk.AGRT_Backend.dto.UserRequestDTO;
import com.unchk.AGRT_Backend.enums.UserRole;
import com.unchk.AGRT_Backend.exceptions.ErrorMessages;
import com.unchk.AGRT_Backend.exceptions.ForbiddenException;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.repositories.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Transactional
    public UserDTO createUser(UserRequestDTO request) {
        validateUserRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ErrorMessages.EMAIL_ALREADY_EXISTS.getMessage());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            throw new RuntimeException(response.toString());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = "ANONYMOUS";

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getCredentials() instanceof String) {
            try {
                String token = (String) authentication.getCredentials();
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtProperties.getSecretKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                currentUserRole = claims.get("role", String.class);
            } catch (Exception e) {
                System.err.println("Error parsing JWT token: " + e.getMessage());
            }
        }

        UserRole requestedRole = request.getRole() != null ? request.getRole() : UserRole.CANDIDATE;

        if (requestedRole == UserRole.ADMIN && !currentUserRole.equals("ADMIN")) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "Only administrators can create admin accounts");
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("errors", errors);
            throw new ForbiddenException(response.toString());
        }

        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            validateProfilePicture(request.getProfilePicture());
        }

        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setProfilePicture(request.getProfilePicture());
            user.setRole(requestedRole);
            User savedUser = userRepository.save(user);
            return UserDTO.class.cast(new UserDTO().toDTO(savedUser));
        } catch (Exception e) {
            throw new RuntimeException("Error creating user");
        }
    }

    private void validateProfilePicture(String base64Image) {
        try {
            String[] parts = base64Image.split(",");
            String base64Data = parts.length > 1 ? parts[1] : parts[0];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            if (imageBytes.length > MAX_IMAGE_SIZE) {
                throw new RuntimeException("IMAGE_TOO_LARGE");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("INVALID_IMAGE_FORMAT");
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    @Transactional
    public User updateUser(String email, UserRequestDTO request) {
        User user = getUserByEmail(email);

        if (!email.equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            validateProfilePicture(request.getProfilePicture());
        }

        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfilePicture(request.getProfilePicture());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = getUserByEmail(email);
        userRepository.delete(user);
    }

    private void validateUserRequest(UserRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        String message = null;

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            message = ErrorMessages.REQUIRED_EMAIL.getMessage();
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            message = ErrorMessages.INVALID_EMAIL_FORMAT.getMessage();
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            message = ErrorMessages.REQUIRED_PASSWORD.getMessage();
        }
        if (request.getPassword() != null && request.getPassword().length() < 6) {
            message = ErrorMessages.PASSWORD_TOO_SHORT.getMessage();

        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            message = ErrorMessages.REQUIRED_FIRSTNAME.getMessage();
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            message = ErrorMessages.REQUIRED_LASTNAME.getMessage();
        }

        errors.put("message", message);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        if (message != null && !message.isEmpty()) {
            throw new RuntimeException(response.toString());
        }
    }

}