package com.unchk.AGRT_Backend.exceptions;

public enum ErrorMessages {
    EMAIL_ALREADY_EXISTS("Un compte avec cet email existe déjà"),
    INVALID_EMAIL_FORMAT("Format d'email invalide"),
    REQUIRED_EMAIL("L'email est requis"),
    REQUIRED_PASSWORD("Le mot de passe est requis"),
    PASSWORD_TOO_SHORT("Le mot de passe doit contenir au moins 6 caractères"),
    REQUIRED_FIRSTNAME("Le prénom est requis"),
    REQUIRED_LASTNAME("Le nom est requis"),
    IMAGE_TOO_LARGE("L'image ne doit pas dépasser 5MB"),
    INVALID_IMAGE_FORMAT("Format d'image invalide"),
    USER_NOT_FOUND("Utilisateur non trouvé");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}