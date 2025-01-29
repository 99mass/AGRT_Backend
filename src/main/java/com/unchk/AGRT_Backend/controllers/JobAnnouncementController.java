package com.unchk.AGRT_Backend.controllers;

import com.unchk.AGRT_Backend.dto.JobAnnouncementDTO;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.services.JobAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-announcements")
@CrossOrigin(origins = "*")
@Tag(name = "Gestion des Annonces d'Emploi", description = "Points d'accès pour la gestion des annonces d'emploi")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class JobAnnouncementController {

    private final JobAnnouncementService jobAnnouncementService;

    @Operation(summary = "Créer une nouvelle annonce d'emploi",
            description = "Crée une nouvelle annonce d'emploi dans le système")
    @PostMapping
    public ResponseEntity<?> createAnnouncement(@RequestBody JobAnnouncementDTO announcementDTO) {
        try {
            JobAnnouncementDTO createdAnnouncement = jobAnnouncementService.createAnnouncement(announcementDTO);
            return new ResponseEntity<>(createdAnnouncement, HttpStatus.CREATED);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Récupérer toutes les annonces d'emploi",
            description = "Retourne la liste de toutes les annonces d'emploi")
    @GetMapping
    public ResponseEntity<?> getAllAnnouncements() {
        try {
            List<JobAnnouncementDTO> announcements = jobAnnouncementService.getAllAnnouncements();
            return ResponseEntity.ok(announcements);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Récupérer une annonce d'emploi par ID",
            description = "Retourne les détails d'une annonce d'emploi spécifique")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnnouncementById(
            @Parameter(description = "ID de l'annonce d'emploi") @PathVariable UUID id) {
        try {
            JobAnnouncementDTO announcement = jobAnnouncementService.getAnnouncementById(id);
            return ResponseEntity.ok(announcement);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Mettre à jour une annonce d'emploi",
            description = "Met à jour les informations d'une annonce d'emploi existante")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnouncement(
            @Parameter(description = "ID de l'annonce d'emploi") @PathVariable UUID id,
            @RequestBody JobAnnouncementDTO announcementDTO) {
        try {
            JobAnnouncementDTO updatedAnnouncement = jobAnnouncementService.updateAnnouncement(id, announcementDTO);
            return ResponseEntity.ok(updatedAnnouncement);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Supprimer une annonce d'emploi",
            description = "Supprime une annonce d'emploi du système")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(
            @Parameter(description = "ID de l'annonce d'emploi") @PathVariable UUID id) {
        try {
            jobAnnouncementService.deleteAnnouncement(id);
            return ResponseEntity.noContent().build();
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }
}