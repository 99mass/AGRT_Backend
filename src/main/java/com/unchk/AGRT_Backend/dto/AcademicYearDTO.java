package com.unchk.AGRT_Backend.dto;

import com.unchk.AGRT_Backend.enums.AcademicYearStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearDTO {
    private UUID id;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private AcademicYearStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}