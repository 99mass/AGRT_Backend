package com.unchk.AGRT_Backend.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.unchk.AGRT_Backend.models.AcademicYear;
import com.unchk.AGRT_Backend.enums.AcademicYearStatus;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    // Rechercher par nom d'année académique
    Optional<AcademicYear> findByYearName(String yearName);
    
    // Vérifier si une année académique existe déjà
    boolean existsByYearName(String yearName);
    
    // Trouver les années académiques actives
    List<AcademicYear> findByStatus(AcademicYearStatus status);
    
    // Trouver les années académiques qui chevauchent une période donnée
    List<AcademicYear> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        LocalDate endDate, LocalDate startDate);
    
    // Trouver les années académiques qui commencent après une date donnée
    List<AcademicYear> findByStartDateAfter(LocalDate date);
    
    // Trouver les années académiques qui se terminent avant une date donnée
    List<AcademicYear> findByEndDateBefore(LocalDate date);
}