package com.unchk.AGRT_Backend.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.unchk.AGRT_Backend.services.ApplicationService;
import com.unchk.AGRT_Backend.dto.ApplicationDTO;
import com.unchk.AGRT_Backend.dto.ApplicationWithDocumentsDTO;
// import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// import java.util.List;
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

    @Operation(summary = "Obtenir une candidature par son ID", description = "Récupère les détails d'une candidature spécifique", responses = {
            @ApiResponse(responseCode = "200", description = "Candidature trouvée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getApplication(
            @Parameter(description = "ID de la candidature", required = true) @PathVariable UUID id) {
        try {
            ApplicationDTO application = applicationService.getApplicationById(id);
            return ResponseEntity.ok(application);
        } catch (UserServiceException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // @Operation(summary = "Mettre à jour le statut d'une candidature", description
    // = "Permet de modifier le statut d'une candidature existante", responses = {
    // @ApiResponse(responseCode = "200", description = "Statut mis à jour avec
    // succès", content = @Content(mediaType = "application/json", schema =
    // @Schema(implementation = ApplicationDTO.class))),
    // @ApiResponse(responseCode = "404", description = "Candidature non trouvée",
    // content = @Content)
    // })
    // @PutMapping("/{id}/status")
    // public ResponseEntity<ApplicationDTO> updateApplicationStatus(
    // @Parameter(description = "ID de la candidature", required = true)
    // @PathVariable UUID id,

    // @Parameter(description = "Nouveau statut", required = true) @RequestParam
    // ApplicationStatus status,

    // @Parameter(description = "Commentaires sur le changement de statut")
    // @RequestParam(required = false) String comments) {
    // try {
    // ApplicationDTO updatedApplication =
    // applicationService.updateApplicationStatus(id, status, comments);
    // return ResponseEntity.ok(updatedApplication);
    // } catch (UserServiceException e) {
    // return ResponseEntity.status(e.getStatus()).build();
    // }
    // }

    // @Operation(summary = "Obtenir toutes les candidatures", description =
    // "Récupère la liste de toutes les candidatures", responses = {
    // @ApiResponse(responseCode = "200", description = "Liste des candidatures
    // récupérée avec succès", content = @Content(mediaType = "application/json",
    // schema = @Schema(implementation = ApplicationDTO.class)))
    // })
    // @GetMapping
    // public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
    // return ResponseEntity.ok(applicationService.getAllApplications());
    // }

    // @Operation(summary = "Obtenir les candidatures par candidat", description =
    // "Récupère toutes les candidatures d'un candidat spécifique", responses = {
    // @ApiResponse(responseCode = "200", description = "Liste des candidatures du
    // candidat récupérée avec succès", content = @Content(mediaType =
    // "application/json", schema = @Schema(implementation = ApplicationDTO.class)))
    // })
    // @GetMapping("/candidate/{candidateId}")
    // public ResponseEntity<List<ApplicationDTO>> getApplicationsByCandidate(
    // @Parameter(description = "ID du candidat", required = true) @PathVariable
    // UUID candidateId) {
    // return
    // ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidateId));
    // }

    // @Operation(summary = "Obtenir les candidatures par annonce", description =
    // "Récupère toutes les candidatures pour une annonce spécifique", responses = {
    // @ApiResponse(responseCode = "200", description = "Liste des candidatures pour
    // l'annonce récupérée avec succès", content = @Content(mediaType =
    // "application/json", schema = @Schema(implementation = ApplicationDTO.class)))
    // })
    // @GetMapping("/announcement/{announcementId}")
    // public ResponseEntity<List<ApplicationDTO>> getApplicationsByAnnouncement(
    // @Parameter(description = "ID de l'annonce", required = true) @PathVariable
    // UUID announcementId) {
    // return
    // ResponseEntity.ok(applicationService.getApplicationsByAnnouncement(announcementId));
    // }

    // @Operation(summary = "Vérifier si une candidature est complète", description
    // = "Vérifie si tous les documents requis sont présents et valides", responses
    // = {
    // @ApiResponse(responseCode = "200", description = "Statut de complétude
    // vérifié avec succès"),
    // @ApiResponse(responseCode = "404", description = "Candidature non trouvée",
    // content = @Content)
    // })
    // @GetMapping("/{id}/complete")
    // public ResponseEntity<Boolean> isApplicationComplete(
    // @Parameter(description = "ID de la candidature", required = true)
    // @PathVariable UUID id) {
    // try {
    // boolean isComplete = applicationService.isApplicationComplete(id);
    // return ResponseEntity.ok(isComplete);
    // } catch (UserServiceException e) {
    // return ResponseEntity.notFound().build();
    // }
    // }

    // @Operation(summary = "Rechercher des candidatures", description = "Recherche
    // des candidatures selon différents critères", responses = {
    // @ApiResponse(responseCode = "200", description = "Résultats de la recherche",
    // content = @Content(mediaType = "application/json", schema =
    // @Schema(implementation = ApplicationDTO.class)))
    // })
    // @GetMapping("/search")
    // public ResponseEntity<List<ApplicationDTO>> searchApplications(
    // @Parameter(description = "Terme de recherche") @RequestParam String query) {
    // return ResponseEntity.ok(applicationService.searchApplications(query));
    // }

    // @Operation(summary = "Obtenir les candidatures par année académique",
    // description = "Récupère toutes les candidatures pour une année académique
    // spécifique", responses = {
    // @ApiResponse(responseCode = "200", description = "Liste des candidatures pour
    // l'année académique récupérée avec succès", content = @Content(mediaType =
    // "application/json", schema = @Schema(implementation = ApplicationDTO.class)))
    // })
    // @GetMapping("/academic-year/{academicYearId}")
    // public ResponseEntity<List<ApplicationDTO>> getApplicationsByAcademicYear(
    // @Parameter(description = "ID de l'année académique", required = true)
    // @PathVariable UUID academicYearId) {
    // return
    // ResponseEntity.ok(applicationService.getApplicationsByAcademicYear(academicYearId));
    // }
}