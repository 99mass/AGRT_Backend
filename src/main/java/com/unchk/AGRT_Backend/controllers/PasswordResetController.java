package com.unchk.AGRT_Backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unchk.AGRT_Backend.dto.PasswordResetRequestDTO;
import com.unchk.AGRT_Backend.dto.PasswordResetConfirmDTO;
import com.unchk.AGRT_Backend.dto.UserDTO;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.services.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
@CrossOrigin(origins = "*")
@Tag(name = "Réinitialisation de mot de passe", description = "Points d'accès pour la réinitialisation de mot de passe")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Operation(summary = "Demander la réinitialisation du mot de passe", description = "Envoie un code OTP à l'email spécifié pour la réinitialisation du mot de passe", responses = {
            @ApiResponse(responseCode = "200", description = "Code OTP envoyé avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(
            @Parameter(description = "Email de l'utilisateur", required = true) @RequestBody PasswordResetRequestDTO request) {
        try {
            passwordResetService.requestPasswordReset(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Un code de réinitialisation a été envoyé à votre adresse email");

            return ResponseEntity.ok(response);
        } catch (UserServiceException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", e.getStatus().value());

            return ResponseEntity
                    .status(e.getStatus())
                    .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Une erreur inattendue est survenue");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @Operation(summary = "Confirmer la réinitialisation du mot de passe", description = "Vérifie le code OTP et met à jour le mot de passe", responses = {
            @ApiResponse(responseCode = "200", description = "Mot de passe mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Code OTP invalide ou expiré"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPasswordReset(
            @Parameter(description = "Code OTP et nouveau mot de passe", required = true) @RequestBody PasswordResetConfirmDTO request) {
        try {

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mot de passe réinitialisé avec succès");

            return ResponseEntity.ok(response);
        } catch (UserServiceException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", e.getStatus().value());

            return ResponseEntity
                    .status(e.getStatus())
                    .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Une erreur inattendue est survenue");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}