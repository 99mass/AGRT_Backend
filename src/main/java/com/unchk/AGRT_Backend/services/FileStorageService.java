package com.unchk.AGRT_Backend.services;
import java.nio.file.Path;
import java.io.IOException;

public interface FileStorageService {
    String storeFile(String base64Image, String filename) throws IOException;
    void deleteFile(String filename);
    Path getFilePath(String filename);
}