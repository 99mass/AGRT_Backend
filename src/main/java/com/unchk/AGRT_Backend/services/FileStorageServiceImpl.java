package com.unchk.AGRT_Backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeFile(String base64Image, String originalFilename) throws IOException {
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String filename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        // Décoder et sauvegarder l'image
        String[] parts = base64Image.split(",");
        String base64Data = parts.length > 1 ? parts[1] : parts[0];
        byte[] imageData = Base64.getDecoder().decode(base64Data);
        Files.write(filePath, imageData);

        return filename;
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path filePath = getFilePath(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Gérer l'exception
        }
    }

    @Override
    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }
}