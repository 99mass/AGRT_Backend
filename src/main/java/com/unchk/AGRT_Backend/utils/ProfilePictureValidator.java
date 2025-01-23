package com.unchk.AGRT_Backend.utils;

import org.springframework.http.HttpStatus;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilePictureValidator {
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 Mo

    private static final List<String> ALLOWED_BASE64_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/jpg"
    );

    public static void validateProfilePicture(String base64Image) throws UserServiceException {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new UserServiceException("Aucune image n'a été fournie", HttpStatus.BAD_REQUEST);
        }

        // Vérifier le format base64
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new UserServiceException("Format d'image base64 invalide", HttpStatus.BAD_REQUEST);
        }

        // Vérifier le type MIME
        String mimeType = parts[0].split(":")[1].split(";")[0];
        if (!ALLOWED_BASE64_MIME_TYPES.contains(mimeType)) {
            throw new UserServiceException(
                "Type de fichier non autorisé. Les types autorisés sont : " + 
                String.join(", ", ALLOWED_BASE64_MIME_TYPES.stream()
                    .map(type -> type.replace("image/", ""))
                    .collect(Collectors.toList())), 
                HttpStatus.BAD_REQUEST
            );
        }

        // Décoder l'image base64
        try {
            // Supprimer les métadonnées base64
            String base64Data = parts[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Vérifier la taille
            if (imageBytes.length > MAX_IMAGE_SIZE) {
                throw new UserServiceException(
                    String.format("La taille de l'image ne doit pas dépasser %d Mo", MAX_IMAGE_SIZE / (1024 * 1024)), 
                    HttpStatus.BAD_REQUEST
                );
            }

            // Vérifier si c'est une image valide
            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
                BufferedImage image = ImageIO.read(bis);
                if (image == null) {
                    throw new UserServiceException(
                        "Le fichier n'est pas une image valide ou est corrompu", 
                        HttpStatus.BAD_REQUEST
                    );
                }

                // Vérification des dimensions minimales
                if (image.getWidth() < 10 || image.getHeight() < 10) {
                    throw new UserServiceException(
                        "Les dimensions de l'image sont trop petites", 
                        HttpStatus.BAD_REQUEST
                    );
                }
            }
        } catch (IllegalArgumentException e) {
            throw new UserServiceException("Données base64 invalides", HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            throw new UserServiceException("Erreur lors de la lecture de l'image", HttpStatus.BAD_REQUEST);
        }
    }
}