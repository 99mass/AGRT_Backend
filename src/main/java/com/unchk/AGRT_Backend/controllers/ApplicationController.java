package com.unchk.AGRT_Backend.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;

import com.unchk.AGRT_Backend.services.ApplicationService;
import com.unchk.AGRT_Backend.dto.ApplicationDTO;
import com.unchk.AGRT_Backend.dto.ApplicationDetailDTO;
import com.unchk.AGRT_Backend.dto.ApplicationWithDocumentsDTO;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
@Tag(name = "Gestion des Candidatures", description = "API pour la gestion des candidatures")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Operation(summary = "Créer une candidature avec des documents", description = "Permet de créer une candidature avec ses documents en une seule requête", responses = {
            @ApiResponse(responseCode = "201", description = "Candidature créée avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Annonce ou candidat non trouvé", content = @Content)
    })

    @PostMapping("/with-documents")
    public ResponseEntity<?> createApplicationWithDocuments(
            @RequestBody ApplicationWithDocumentsDTO applicationWithDocumentsDTO) {
        try {
            ApplicationDTO createdApplication = applicationService
                    .createApplicationWithDocuments(applicationWithDocumentsDTO);
            return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
        } catch (UserServiceException e) {
            // Renvoyer le message d'erreur avec le code 400
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/announcement/{announcementId}")
    @Operation(summary = "Récupérer toutes les candidatures pour une annonce spécifique")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationDetailDTO>> getApplicationsByAnnouncement(
            @PathVariable UUID announcementId) {
        try {
            List<ApplicationDetailDTO> applications = applicationService
                    .getApplicationsByAnnouncementWithDocuments(announcementId);
            return ResponseEntity.ok(applications);
        } catch (UserServiceException e) {
            throw new ResponseStatusException(e.getStatus(), e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une candidature par son ID avec tous ses documents")
    @ApiResponse(responseCode = "200", description = "Candidature trouvée avec ses documents")
    @ApiResponse(responseCode = "404", description = "Candidature non trouvée")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApplicationDetailDTO> getApplicationById(@PathVariable UUID id) {
        try {
            ApplicationDetailDTO application = applicationService.getApplicationByIdWithDocuments(id);
            return ResponseEntity.ok(application);
        } catch (UserServiceException e) {
            throw new ResponseStatusException(e.getStatus(), e.getMessage());
        }
    }
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une candidature", description = "Permet de modifier une candidature existante si la date de fin n'est pas atteinte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidature mise à jour avec succès", content = @Content(schema = @Schema(implementation = ApplicationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide ou période de candidature terminée"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur lors de la mise à jour")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateApplication(
            @PathVariable UUID id,
            @RequestBody ApplicationWithDocumentsDTO updateDTO) {
        try {
            ApplicationDTO updatedApplication = applicationService.updateApplication(id, updateDTO);
            return ResponseEntity.ok(updatedApplication);
        } catch (UserServiceException e) {
            return ResponseEntity
                    .status(e.getStatus())
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Une erreur inattendue s'est produite"));
        }
    }

    @DeleteMapping("/{applicationId}/documents/{documentId}")
    @Operation(summary = "Supprimer un document d'une candidature")
    @ApiResponse(responseCode = "204", description = "Document supprimé avec succès")
    @ApiResponse(responseCode = "404", description = "Document ou candidature non trouvé")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId) {
        applicationService.removeDocumentFromApplication(applicationId, documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/complete")
    @Operation(summary = "Vérifier si une candidature est complète")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Boolean> isApplicationComplete(@PathVariable UUID id) {
        boolean isComplete = applicationService.isApplicationComplete(id);
        return ResponseEntity.ok(isComplete);
    }

}