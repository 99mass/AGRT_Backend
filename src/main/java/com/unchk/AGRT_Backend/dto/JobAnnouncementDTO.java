package com.unchk.AGRT_Backend.dto;

import com.unchk.AGRT_Backend.enums.AnnouncementStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAnnouncementDTO {
    private UUID id;
    private UUID academicYearId;
    private String title;
    private String description;
    private AnnouncementStatus status;
    private LocalDateTime publicationDate;
    private LocalDateTime closingDate;
    private UUID createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}