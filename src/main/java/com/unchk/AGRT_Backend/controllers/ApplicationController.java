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
import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApplicationDetailDTO> getApplicationById(@PathVariable UUID id) {
        try {
            ApplicationDetailDTO application = applicationService.getApplicationByIdWithDocuments(id);
            return ResponseEntity.ok(application);
        } catch (UserServiceException e) {
            throw new ResponseStatusException(e.getStatus(), e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une candidature", description = "Permet à un administrateur de modifier le statut d'une candidature et d'ajouter des commentaires")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationDetailDTO.class), examples = @ExampleObject(value = """
                    {
                        "id": "44dca219-a7fd-41a8-b16e-839c2ddcd384",
                        "candidateId": "550e8400-e29b-41d4-a716-446655440000",
                        "announcementId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                        "academicYearId": "91c27c34-e775-4c4e-806c-721c34a7bfb9",
                        "status": "UNDER_REVIEW",
                        "applicationType": "FULL_TIME",
                        "createdAt": "2024-02-17T10:30:00",
                        "updatedAt": "2024-02-17T14:45:00",
                        "documents": [
                            {
                                "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                "fileName": "CV_2024.pdf",
                                "filePath": "/api/documents/CV_2024.pdf",
                                "documentType": "CV",
                                "status": "VALID",
                                "uploadDate": "2024-02-17T10:35:00"
                            }
                        ]
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "message": "Statut invalide"
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "message": "Accès refusé"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "message": "Candidature non trouvée"
                    }
                    """)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatusUpdateRequest.class), examples = @ExampleObject(value = """
            {
                "status": "UNDER_REVIEW",
                "comments": "La candidature a été mise en cours de révision"
            }
            """)))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateApplicationStatus(
            @Parameter(description = "ID de la candidature", required = true) @PathVariable UUID id,
            @RequestBody Map<String, Object> updateRequest) {
        try {
            ApplicationStatus newStatus = ApplicationStatus.valueOf((String) updateRequest.get("status"));
            String comments = (String) updateRequest.getOrDefault("comments", "");

            @SuppressWarnings("unused")
            ApplicationDTO updatedApplication = applicationService.updateApplicationStatus(id, newStatus, comments);
            ApplicationDetailDTO applicationDetail = applicationService.getApplicationByIdWithDocuments(id);

            return ResponseEntity.ok(applicationDetail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Statut invalide"));
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

    @Schema(description = "Requête de mise à jour du statut")
    class StatusUpdateRequest {
        @Schema(description = "Nouveau statut de la candidature", example = "UNDER_REVIEW", required = true)
        private String status;

        @Schema(description = "Commentaires sur le changement de statut", example = "La candidature a été mise en cours de révision")
        private String comments;
    }

    @Operation(summary = "Mettre à jour une candidature", description = "Permet de modifier une candidature existante si la date de fin n'est pas atteinte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidature mise à jour avec succès", content = @Content(schema = @Schema(implementation = ApplicationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide ou période de candidature terminée"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur lors de la mise à jour")
    })
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(
            @PathVariable UUID id,
            @RequestBody ApplicationWithDocumentsDTO updateDTO) {
        try {
            ApplicationDetailDTO updatedApplication = applicationService.updateApplication(id, updateDTO);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId) {
        applicationService.removeDocumentFromApplication(applicationId, documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/complete")
    @Operation(summary = "Vérifier si une candidature est complète")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<Boolean> isApplicationComplete(@PathVariable UUID id) {
        boolean isComplete = applicationService.isApplicationComplete(id);
        return ResponseEntity.ok(isComplete);
    }

    @GetMapping("/user")
    @Operation(summary = "Récupérer toutes les candidatures de l'utilisateur actuel", description = "Renvoie toutes les candidatures de l'utilisateur connecté avec leurs documents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des candidatures récupérée avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationDetailDTO>> getUserApplications() {
        try {
            List<ApplicationDetailDTO> applications = applicationService.getApplicationsByCurrentUser();
            return ResponseEntity.ok(applications);
        } catch (UserServiceException e) {
            throw new ResponseStatusException(e.getStatus(), e.getMessage());
        }
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Annuler une candidature", description = "Permet à un candidat d'annuler sa candidature en supprimant tous les documents associés et la candidature elle-même")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Candidature annulée avec succès"),
            @ApiResponse(responseCode = "400", description = "La candidature ne peut plus être annulée"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur lors de l'annulation")
    })
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<?> cancelApplication(@PathVariable UUID id) {
        try {
            applicationService.cancelApplication(id);
            return ResponseEntity.noContent().build();
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
}