package com.unchk.AGRT_Backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unchk.AGRT_Backend.dto.PasswordResetRequestDTO;
import com.unchk.AGRT_Backend.dto.PasswordResetConfirmDTO;
import com.unchk.AGRT_Backend.dto.UserDTO;
import com.unchk.AGRT_Backend.exceptions.ErrorMessages;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.PasswordResetOTP;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.repositories.PasswordResetOTPRepository;
import com.unchk.AGRT_Backend.repositories.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetOTPRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_HOURS = 24;

    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(
                        ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));

        // Vérifier si un OTP actif existe déjà pour cet utilisateur
        Optional<PasswordResetOTP> existingOtp = otpRepository.findByEmailAndIsUsedFalse(email);
        if (existingOtp.isPresent()) {
            // Invalider l'ancien OTP en le marquant comme utilisé
            PasswordResetOTP oldOtp = existingOtp.get();
            oldOtp.setUsed(true);
            otpRepository.save(oldOtp);
        }

        // Générer un nouveau code OTP
        String otpCode = generateOTP();
        
        // Créer un nouvel enregistrement OTP
        PasswordResetOTP otp = new PasswordResetOTP();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiryDate(LocalDateTime.now().plusHours(OTP_EXPIRY_HOURS));
        otp.setCreatedAt(LocalDateTime.now());
        otp.setUsed(false);
        
        otpRepository.save(otp);
        
        // Envoyer l'email avec le code OTP
        emailService.sendPasswordResetEmail(email, user.getFirstName(), otpCode);
    }

    @Transactional
    public UserDTO resetPassword(PasswordResetConfirmDTO request) {
        String email = request.getEmail();
        String otpCode = request.getOtpCode();
        String newPassword = request.getNewPassword();
        
        // Vérifier si le nouveau mot de passe est valide
        if (newPassword == null || newPassword.length() < 6) {
            throw new UserServiceException(
                ErrorMessages.PASSWORD_TOO_SHORT.getMessage(),
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(
                        ErrorMessages.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND));
        
        // Vérifier si le code OTP existe et est valide
        PasswordResetOTP otp = otpRepository.findByOtpCodeAndEmail(otpCode, email)
                .orElseThrow(() -> new UserServiceException(
                        "Code OTP invalide",
                        HttpStatus.BAD_REQUEST));
        
        // Vérifier si le code OTP a déjà été utilisé
        if (otp.isUsed()) {
            throw new UserServiceException(
                "Ce code a déjà été utilisé",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Vérifier si le code OTP a expiré
        if (otp.isExpired()) {
            throw new UserServiceException(
                "Ce code a expiré",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        
        // Marquer l'OTP comme utilisé
        otp.setUsed(true);
        otpRepository.save(otp);
        
        return new UserDTO().toDTO(updatedUser);
    }
    
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            otp.append(CHARACTERS.charAt(index));
        }
        
        return otp.toString();
    }
}