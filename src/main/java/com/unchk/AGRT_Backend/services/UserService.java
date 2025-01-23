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
import com.unchk.AGRT_Backend.utils.ProfilePictureValidator;

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
                // Validation de l'image avant le stockage
                ProfilePictureValidator.validateProfilePicture(request.getProfilePicture());

                profilePicturePath = fileStorageService.storeFile(
                        request.getProfilePicture(),
                        request.getEmail() + "_profile.jpg");
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new UserServiceException(e.getMessage(),
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
                .orElseThrow(() -> new UserServiceException(
                        ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));

        return new UserDTO().toDTO(user);
    }

    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));
        return new UserDTO().toDTO(user);
    }

    @SuppressWarnings("null")
    @Transactional
    public UserDTO updateUser(String email, UserRequestDTO request) {
        // Récupérer l'utilisateur à mettre à jour
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));

        // Récupérer l'authentification courante
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentification requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        String currentUserRole = "CANDIDATE";
        String currentUserEmail = null;

        // Extraire le token et les informations de l'utilisateur courant
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
                currentUserEmail = claims.getSubject(); // Assuming email is the subject
            } catch (Exception e) {
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }

        // Vérifier les permissions de mise à jour
        if (currentUserRole.equals("CANDIDATE") && !currentUserEmail.equals(email)) {
            throw new UserServiceException("Vous n'êtes pas autorisé à modifier ce compte", HttpStatus.FORBIDDEN);
        }

        // Vérification du rôle si un nouveau rôle est fourni
        if (request.getRole() != null) {
            // Vérification si le nouveau rôle est ADMIN
            if (request.getRole() == UserRole.ADMIN) {
                if (!currentUserRole.equals("ADMIN")) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", HttpStatus.FORBIDDEN.value());
                    response.put("errors", "Seuls les administrateurs peuvent mettre à jour le rôle en 'ADMIN'");

                    throw new UserServiceException(response.toString(), HttpStatus.FORBIDDEN);
                }
            }
        }

        // Validation du reste de la requête
        try {
            validateUpdateRequest(request, user);
        } catch (RuntimeException e) {
            throw new UserServiceException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new UserServiceException(ErrorMessages.EMAIL_ALREADY_EXISTS.getMessage(), HttpStatus.BAD_REQUEST);
            }
            user.setEmail(request.getEmail());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update first name if provided
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        // Update last name if provided
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        // Update role if provided and verified
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        // Handle profile picture
        String profilePicturePath = user.getProfilePicture();
        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            try {

                // Validation de l'image avant le stockage
                ProfilePictureValidator.validateProfilePicture(request.getProfilePicture());

                // Delete old profile picture if exists
                if (profilePicturePath != null) {
                    fileStorageService.deleteFile(profilePicturePath);
                }

                // Store new profile picture
                profilePicturePath = fileStorageService.storeFile(
                        request.getProfilePicture(),
                        (request.getEmail() != null ? request.getEmail() : user.getEmail()) + "_profile.jpg");

                user.setProfilePicture(profilePicturePath);
            } catch (IOException e) {
                throw new UserServiceException(e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                throw new UserServiceException(e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Save and return updated user
        User savedUser = userRepository.save(user);
        return new UserDTO().toDTO(savedUser);
    }

    @Transactional
    public void deleteUser(String email) {
        // Récupérer l'utilisateur à supprimer
        User userToDelete = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));

        // Récupérer l'authentification courante
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentification requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        String currentUserRole = "CANDIDATE";
        String currentUserEmail = null;

        // Extraire le token et les informations de l'utilisateur courant
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
                currentUserEmail = claims.getSubject(); // Assuming email is the subject
            } catch (Exception e) {
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }

        // Vérifier les permissions de suppression
        if (currentUserRole.equals("ADMIN")) {
            // Un ADMIN peut supprimer n'importe quel compte
            userRepository.delete(userToDelete);
        } else if (currentUserRole.equals("CANDIDATE")) {
            // Un CANDIDATE ne peut supprimer que son propre compte
            if (currentUserEmail == null || !currentUserEmail.equals(email)) {
                throw new UserServiceException("Vous n'êtes pas autorisé à supprimer ce compte", HttpStatus.FORBIDDEN);
            }

            if (userToDelete.getProfilePicture() != null) {
                try {
                    fileStorageService.deleteFile(userToDelete.getProfilePicture());
                } catch (Exception e) {
                    // Log the error, but don't stop the deletion process
                    System.err.println("Erreur lors de la suppression de l'image de profil : " + e.getMessage());
                }
            }

            userRepository.delete(userToDelete);
        } else {
            // Cas par défaut : accès non autorisé
            throw new UserServiceException("Vous n'êtes pas autorisé à supprimer ce compte", HttpStatus.FORBIDDEN);
        }
    }

    public List<UserDTO> getAllUsers() {
        // Récupérer l'authentification courante
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentification requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        String currentUserRole = "CANDIDATE";

        // Extraire le token et le rôle de l'utilisateur courant
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
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }

        // Vérifier que seul un ADMIN peut récupérer tous les utilisateurs
        if (!currentUserRole.equals("ADMIN")) {
            throw new UserServiceException("Seuls les administrateurs peuvent récupérer la liste des utilisateurs",
                    HttpStatus.FORBIDDEN);
        }

        List<User> users = userRepository.findAll();
        return users.stream().map(user -> new UserDTO().toDTO(user)).collect(Collectors.toList());
    }

    public List<UserDTO> getAllAdmins() {
        // Récupérer l'authentification courante
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentification requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        String currentUserRole = "CANDIDATE";

        // Extraire le token et le rôle de l'utilisateur courant
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
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }

        // Vérifier que seul un ADMIN peut récupérer la liste des administrateurs
        if (!currentUserRole.equals("ADMIN")) {
            throw new UserServiceException("Seuls les administrateurs peuvent récupérer la liste des administrateurs",
                    HttpStatus.FORBIDDEN);
        }

        List<User> candidates = userRepository.findByRole(UserRole.ADMIN);
        return candidates.stream().map(user -> new UserDTO().toDTO(user)).collect(Collectors.toList());
    }

    public List<UserDTO> getAllCandidates() {
        // Récupérer l'authentification courante
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentification requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        String currentUserRole = "CANDIDATE";

        // Extraire le token et le rôle de l'utilisateur courant
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
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }

        // Vérifier que seul un ADMIN peut récupérer la liste des candidats
        if (!currentUserRole.equals("ADMIN")) {
            throw new UserServiceException("Seuls les administrateurs peuvent récupérer la liste des candidats",
                    HttpStatus.FORBIDDEN);
        }

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

    private void validateUpdateRequest(UserRequestDTO request, User existingUser) {
        Map<String, Object> response = new HashMap<>();
        String errors = new String();

        // Email validation (only if provided)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors = ErrorMessages.INVALID_EMAIL_FORMAT.getMessage();
            }
        }

        // Password validation (only if provided)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().length() < 6) {
                errors = ErrorMessages.PASSWORD_TOO_SHORT.getMessage();
            }
        }

        // First name validation (only if provided)
        if (request.getFirstName() != null && request.getFirstName().trim().isEmpty()) {
            errors = ErrorMessages.REQUIRED_FIRSTNAME.getMessage();
        }

        // Last name validation (only if provided)
        if (request.getLastName() != null && request.getLastName().trim().isEmpty()) {
            errors = ErrorMessages.REQUIRED_LASTNAME.getMessage();
        }

        // Role validation (only if provided)
        if (request.getRole() != null
                && !(request.getRole().equals(UserRole.CANDIDATE) || request.getRole().equals(UserRole.ADMIN))) {
            errors = INVALID_ROLE;
        }

        if (!errors.isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            throw new UserServiceException(response.toString(), HttpStatus.BAD_REQUEST);
        }
    }
}