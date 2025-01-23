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
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.repositories.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    // private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    final String FORBIDDEN_MESSAGE = "Seuls les administrateurs peuvent créer des comptes administrateurs.";
    final String INVALID_ROLE = "Le rôle doit être soit 'CANDIDATE' soit 'ADMIN'";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public UserDTO createUser(UserRequestDTO request) {
        try {
            validateUserRequest(request);
        } catch (RuntimeException e) {
            throw new UserServiceException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", ErrorMessages.EMAIL_ALREADY_EXISTS.getMessage());
            throw new RuntimeException(response.toString());
        }

        UserRole requestedRole = request.getRole();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = "CANDIDATE";

        if (requestedRole == UserRole.ADMIN && authentication != null && authentication.isAuthenticated()) {
            String token = null;
            if (authentication.getCredentials() instanceof String) {
                token = (String) authentication.getCredentials();
            }

            if (token != null) {
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtProperties.getSecretKey())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    currentUserRole = claims.get("role", String.class);
                } catch (Exception e) {
                    throw new UserServiceException(FORBIDDEN_MESSAGE, HttpStatus.FORBIDDEN);
                }
            } else {
                throw new UserServiceException(FORBIDDEN_MESSAGE, HttpStatus.FORBIDDEN);
            }
        }

        if (requestedRole == UserRole.ADMIN && !currentUserRole.equals("ADMIN")) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("errors", FORBIDDEN_MESSAGE);

            throw new UserServiceException(response.toString(), HttpStatus.FORBIDDEN);
        }

        String profilePicturePath = null;
        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            try {
                profilePicturePath = fileStorageService.storeFile(
                        request.getProfilePicture(),
                        request.getEmail() + "_profile.jpg");
            } catch (IOException e) {
                throw new UserServiceException("Erreur lors de la sauvegarde de l'image: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                throw new UserServiceException("Erreur inattendue lors de la sauvegarde de l'image: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setProfilePicture(profilePicturePath);
            user.setRole(requestedRole);
            User savedUser = userRepository.save(user);
            return UserDTO.class.cast(new UserDTO().toDTO(savedUser));
        } catch (Exception e) {
            if (profilePicturePath != null) {
                try {
                    fileStorageService.deleteFile(profilePicturePath);
                } catch (Exception ex) {
                }
            }
            throw new UserServiceException("Erreur lors de la création de l'utilisateur",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));
        return UserDTO.class.cast(new UserDTO().toDTO(user));
    }

    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));
        return new UserDTO().toDTO(user);
    }

    @Transactional
    public User updateUser(String email, UserRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(), 
                        HttpStatus.NOT_FOUND));
    
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new UserServiceException(ErrorMessages.EMAIL_ALREADY_EXISTS.getMessage(), HttpStatus.BAD_REQUEST);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));
        userRepository.delete(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> new UserDTO().toDTO(user)).collect(Collectors.toList());
    }

    public List<UserDTO> getAllAdmins() {
        List<User> candidates = userRepository.findByRole(UserRole.ADMIN);
        return candidates.stream().map(user -> new UserDTO().toDTO(user)).collect(Collectors.toList());
    }

    public List<UserDTO> getAllCandidates() {
        List<User> candidates = userRepository.findByRole(UserRole.CANDIDATE);
        return candidates.stream().map(user -> new UserDTO().toDTO(user)).collect(Collectors.toList());
    }

    private void validateUserRequest(UserRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        String errors = new String();

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            errors = (ErrorMessages.REQUIRED_EMAIL.getMessage());
        } else if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors = ErrorMessages.INVALID_EMAIL_FORMAT.getMessage();
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors = ErrorMessages.REQUIRED_PASSWORD.getMessage();
        } else if (request.getPassword().length() < 6) {
            errors = ErrorMessages.PASSWORD_TOO_SHORT.getMessage();
        }

        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            errors = ErrorMessages.REQUIRED_FIRSTNAME.getMessage();
        }

        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            errors = ErrorMessages.REQUIRED_LASTNAME.getMessage();
        }

        if (request.getRole() == null
                || !(request.getRole().equals(UserRole.CANDIDATE) || request.getRole().equals(UserRole.ADMIN))) {
            errors = INVALID_ROLE;
        }

        if (!errors.isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            throw new UserServiceException(response.toString(), HttpStatus.BAD_REQUEST);
        }
    }

}