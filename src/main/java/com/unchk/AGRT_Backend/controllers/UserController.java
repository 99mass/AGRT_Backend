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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;

@Tag(name = "Gestion des Utilisateurs", description = "Points d'accès pour la gestion des utilisateurs")
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    final String CREATED = "Utilisateur créé avec succès.";

    @Autowired
    private UserService userService;

    // @PostMapping
    // public ResponseEntity<String> createUser(@Valid @RequestBody UserRequestDTO
    // request) {

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

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserDTO>> getAllAdmins() {
        List<UserDTO> users = userService.getAllAdmins();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<UserDTO>> getAllCandidates() {
        List<UserDTO> candidates = userService.getAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{email}")
    public ResponseEntity<User> updateUser(
            @PathVariable String email,
            @Valid @RequestBody UserRequestDTO request) {
        User updatedUser = userService.updateUser(email, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}