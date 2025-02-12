package com.unchk.AGRT_Backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unchk.AGRT_Backend.models.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByCandidateId(UUID candidateId);

    List<Application> findByAnnouncementId(UUID announcementId);

    List<Application> findByAcademicYearId(UUID academicYearId);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.documents WHERE a.id = :id")
    Optional<Application> findByIdWithDocuments(@Param("id") UUID id);

    boolean existsByCandidateIdAndAnnouncementId(UUID candidateId, UUID announcementId);

    @Query("SELECT a FROM Application a WHERE " +
            "LOWER(a.candidate.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.candidate.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.candidate.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Application> searchApplications(@Param("query") String query);
}