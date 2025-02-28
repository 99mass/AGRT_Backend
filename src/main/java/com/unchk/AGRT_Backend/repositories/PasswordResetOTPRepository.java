package com.unchk.AGRT_Backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unchk.AGRT_Backend.models.PasswordResetOTP;

import java.util.UUID;

@Repository
public interface PasswordResetOTPRepository extends JpaRepository<PasswordResetOTP, UUID> {
    Optional<PasswordResetOTP> findByOtpCodeAndEmail(String otpCode, String email);

    Optional<PasswordResetOTP> findByEmailAndIsUsedFalse(String email);
}