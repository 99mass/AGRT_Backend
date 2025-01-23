package com.unchk.AGRT_Backend.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unchk.AGRT_Backend.dto.UserDTO;
import com.unchk.AGRT_Backend.dto.UserRequestDTO;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Gestion des Utilisateurs", description = "Points d'accès pour la gestion des utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    final String CREATED = "Utilisateur créé avec succès.";

    @Autowired
    private UserService userService;

    @Operation(summary = "Créer un nouvel utilisateur", description = "Point d'accès pour l'enregistrement d'un nouvel utilisateur dans le système", responses = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données utilisateur invalides")
    })
    @PostMapping
    public ResponseEntity<String> createUser(
            @Parameter(description = "Détails de l'inscription de l'utilisateur", required = true) @Valid @RequestBody UserRequestDTO request) {
        try {
            userService.createUser(request);
            return new ResponseEntity<>(CREATED, HttpStatus.CREATED);
        } catch (UserServiceException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    @Operation(summary = "Récupérer tous les utilisateurs", description = "Retourne une liste complète de tous les utilisateurs du système", responses = {
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Récupérer tous les administrateurs", description = "Retourne une liste de tous les utilisateurs avec le rôle administrateur", responses = {
            @ApiResponse(responseCode = "200", description = "Liste des administrateurs récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    })
    @GetMapping("/admins")
    public ResponseEntity<List<UserDTO>> getAllAdmins() {
        List<UserDTO> users = userService.getAllAdmins();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Récupérer tous les candidats", description = "Retourne une liste de tous les utilisateurs avec le rôle candidat", responses = {
            @ApiResponse(responseCode = "200", description = "Liste des candidats récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    })
    @GetMapping("/candidates")
    public ResponseEntity<List<UserDTO>> getAllCandidates() {
        List<UserDTO> candidates = userService.getAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @Operation(summary = "Récupérer un utilisateur par ID", description = "Retourne les détails d'un utilisateur spécifique en utilisant son identifiant", responses = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(name = "ID de l'utilisateur", description = "Identifiant unique de l'utilisateur", required = true) @PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Mettre à jour un utilisateur", description = "Permet de mettre à jour les informations d'un utilisateur existant", responses = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Données de mise à jour invalides"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(name = "ID de l'utilisateur", description = "Identifiant unique de l'utilisateur", required = true) @PathVariable String email,
            @Parameter(name = "Données de mise à jour", description = "Nouvelles informations de l'utilisateur", required = true) @Valid @RequestBody UserRequestDTO request) {
        User updatedUser = userService.updateUser(email, request);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur du système à partir de son ID", responses = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(name = "ID de l'utilisateur", description = "Identifiant unique de l'utilisateur", required = true) @PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}