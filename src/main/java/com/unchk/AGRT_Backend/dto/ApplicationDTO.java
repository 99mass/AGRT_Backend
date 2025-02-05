package com.unchk.AGRT_Backend.dto;

import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.ApplicationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApplicationDTO {
    private UUID id;
    private UUID candidateId;
    private UUID announcementId;
    private UUID academicYearId;
    private ApplicationType applicationType;
    private ApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}