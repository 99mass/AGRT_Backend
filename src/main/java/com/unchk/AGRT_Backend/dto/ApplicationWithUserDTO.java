package com.unchk.AGRT_Backend.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.ApplicationType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationWithUserDTO {
    private UUID id;
    private UUID candidateId;
    private UUID announcementId;
    private UUID academicYearId;
    private ApplicationType applicationType;
    private ApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<DocumentResponseDTO> documents;
    
    // Informations utilisateur
    private UserDTO candidate;
}