package com.unchk.AGRT_Backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.ApplicationType;

@Data
public class ApplicationDetailDTO {
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
}