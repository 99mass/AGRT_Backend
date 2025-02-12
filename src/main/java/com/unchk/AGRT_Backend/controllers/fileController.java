package com.unchk.AGRT_Backend.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.unchk.AGRT_Backend.services.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
@Tag(name = "Gestion des Images et Fichiers", description = "Points d'accès pour la gestion des images et fichiers")
public class fileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Operation(summary = "Récupérer une image", description = "Retourne une image à partir de son nom de fichier", responses = {
            @ApiResponse(responseCode = "200", description = "Image trouvée et retournée", content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "404", description = "Image non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "Nom du fichier image", required = true) @PathVariable String filename) {
        try {
            Path filePath = fileStorageService.getFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Récupérer un document", description = "Retourne un document (pdf) à partir de son nom de fichier", responses = {
            @ApiResponse(responseCode = "200", description = "Document trouvé et retourné", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Document non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/documents/{filename:.+}")
    public ResponseEntity<Resource> getDocument(
            @Parameter(description = "Nom du fichier document", required = true) @PathVariable String filename) {
        try {

            Path filePath = fileStorageService.getFilePathDocument(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
