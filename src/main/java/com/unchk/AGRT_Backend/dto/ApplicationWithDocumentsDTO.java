package com.unchk.AGRT_Backend.dto;

import com.unchk.AGRT_Backend.enums.ApplicationType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ApplicationWithDocumentsDTO {
    // private UUID candidateId;
    private UUID announcementId;
    private UUID academicYearId;
    private ApplicationType applicationType;
    private List<DocumentDTO> documents;
}
