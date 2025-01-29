package com.unchk.AGRT_Backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unchk.AGRT_Backend.dto.AcademicYearDTO;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.services.AcademicYearService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic-years")
@CrossOrigin(origins = "*")
@Tag(name = "Gestion des Années Académiques", description = "Points d'accès pour la gestion des années académiques")
@SecurityRequirement(name = "bearerAuth")
public class AcademicYearController {

    @Autowired
    private AcademicYearService academicYearService;

    @Operation(summary = "Créer une nouvelle année académique", 
               description = "Crée une nouvelle année académique dans le système (réservé aux administrateurs)")
    @PostMapping
    public ResponseEntity<?> createAcademicYear(@RequestBody AcademicYearDTO academicYearDTO) {
        try {
            AcademicYearDTO createdYear = academicYearService.createAcademicYear(academicYearDTO);
            return new ResponseEntity<>(createdYear, HttpStatus.CREATED);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Récupérer toutes les années académiques", 
               description = "Retourne la liste de toutes les années académiques (réservé aux administrateurs)")
    @GetMapping
    public ResponseEntity<?> getAllAcademicYears() {
        try {
            List<AcademicYearDTO> years = academicYearService.getAllAcademicYears();
            return ResponseEntity.ok(years);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Récupérer une année académique par ID", 
               description = "Retourne les détails d'une année académique spécifique (réservé aux administrateurs)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAcademicYearById(
            @Parameter(description = "ID de l'année académique") @PathVariable UUID id) {
        try {
            AcademicYearDTO year = academicYearService.getAcademicYearById(id);
            return ResponseEntity.ok(year);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Mettre à jour une année académique", 
               description = "Met à jour les informations d'une année académique existante (réservé aux administrateurs)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAcademicYear(
            @Parameter(description = "ID de l'année académique") @PathVariable UUID id,
            @RequestBody AcademicYearDTO academicYearDTO) {
        try {
            AcademicYearDTO updatedYear = academicYearService.updateAcademicYear(id, academicYearDTO);
            return ResponseEntity.ok(updatedYear);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }

    @Operation(summary = "Supprimer une année académique", 
               description = "Supprime une année académique du système (réservé aux administrateurs)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAcademicYear(
            @Parameter(description = "ID de l'année académique") @PathVariable UUID id) {
        try {
            academicYearService.deleteAcademicYear(id);
            return ResponseEntity.noContent().build();
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", e.getStatus().value());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, e.getStatus());
        }
    }
}