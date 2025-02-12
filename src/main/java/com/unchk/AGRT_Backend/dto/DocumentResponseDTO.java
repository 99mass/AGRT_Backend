package com.unchk.AGRT_Backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.unchk.AGRT_Backend.enums.DocumentStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;

import lombok.Data;

@Data
public class DocumentResponseDTO {
    private UUID id;
    private String fileName;
    private String filePath;
    private DocumentType documentType;
    private DocumentStatus status;
    private LocalDateTime uploadDate;
}