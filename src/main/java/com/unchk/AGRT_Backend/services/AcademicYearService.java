package com.unchk.AGRT_Backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.AcademicYear;
import com.unchk.AGRT_Backend.repositories.AcademicYearRepository;
import com.unchk.AGRT_Backend.config.JwtProperties;
import com.unchk.AGRT_Backend.dto.AcademicYearDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AcademicYearService {

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private JwtProperties jwtProperties;

    private void verifyAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentication requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication.getCredentials() instanceof String) {
            token = (String) authentication.getCredentials();
        }

        if (token != null) {
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                String role = claims.get("role", String.class);
                if (!"ADMIN".equals(role)) {
                    throw new UserServiceException("Accès restreint aux administrateurs", HttpStatus.FORBIDDEN);
                }
            } catch (Exception e) {
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }
    }

    @Transactional
    public AcademicYearDTO createAcademicYear(AcademicYearDTO academicYearDTO) {
        verifyAdminAccess();
        
        if (academicYearRepository.existsByYearName(academicYearDTO.getYearName())) {
            throw new UserServiceException("Cette année académique existe déjà", HttpStatus.BAD_REQUEST);
        }

        validateAcademicYear(academicYearDTO);

        AcademicYear academicYear = new AcademicYear();
        academicYear.setYearName(academicYearDTO.getYearName());
        academicYear.setStartDate(academicYearDTO.getStartDate());
        academicYear.setEndDate(academicYearDTO.getEndDate());
        academicYear.setStatus(academicYearDTO.getStatus());

        AcademicYear savedYear = academicYearRepository.save(academicYear);
        return convertToDTO(savedYear);
    }

    public List<AcademicYearDTO> getAllAcademicYears() {
        return academicYearRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public AcademicYearDTO getAcademicYearById(UUID id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
            .orElseThrow(() -> new UserServiceException("Année académique non trouvée", HttpStatus.NOT_FOUND));
        return convertToDTO(academicYear);
    }

    @Transactional
    public AcademicYearDTO updateAcademicYear(UUID id, AcademicYearDTO academicYearDTO) {
        verifyAdminAccess();
        
        AcademicYear existingYear = academicYearRepository.findById(id)
            .orElseThrow(() -> new UserServiceException("Année académique non trouvée", HttpStatus.NOT_FOUND));

        if (!existingYear.getYearName().equals(academicYearDTO.getYearName()) &&
            academicYearRepository.existsByYearName(academicYearDTO.getYearName())) {
            throw new UserServiceException("Cette année académique existe déjà", HttpStatus.BAD_REQUEST);
        }

        validateAcademicYear(academicYearDTO);

        existingYear.setYearName(academicYearDTO.getYearName());
        existingYear.setStartDate(academicYearDTO.getStartDate());
        existingYear.setEndDate(academicYearDTO.getEndDate());
        existingYear.setStatus(academicYearDTO.getStatus());

        AcademicYear updatedYear = academicYearRepository.save(existingYear);
        return convertToDTO(updatedYear);
    }

    @Transactional
    public void deleteAcademicYear(UUID id) {
        verifyAdminAccess();
        if (!academicYearRepository.existsById(id)) {
            throw new UserServiceException("Année académique non trouvée", HttpStatus.NOT_FOUND);
        }
        academicYearRepository.deleteById(id);
    }

    private void validateAcademicYear(AcademicYearDTO academicYearDTO) {
        if (academicYearDTO.getStartDate() == null || academicYearDTO.getEndDate() == null) {
            throw new UserServiceException("Les dates de début et de fin sont requises", HttpStatus.BAD_REQUEST);
        }
        if (academicYearDTO.getEndDate().isBefore(academicYearDTO.getStartDate())) {
            throw new UserServiceException("La date de fin doit être postérieure à la date de début", HttpStatus.BAD_REQUEST);
        }
        if (academicYearDTO.getStatus() == null) {
            throw new UserServiceException("Le statut est requis", HttpStatus.BAD_REQUEST);
        }
    }

    private AcademicYearDTO convertToDTO(AcademicYear academicYear) {
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setId(academicYear.getId());
        dto.setYearName(academicYear.getYearName());
        dto.setStartDate(academicYear.getStartDate());
        dto.setEndDate(academicYear.getEndDate());
        dto.setStatus(academicYear.getStatus());
        dto.setCreatedAt(academicYear.getCreatedAt());
        dto.setUpdatedAt(academicYear.getUpdatedAt());
        return dto;
    }
}