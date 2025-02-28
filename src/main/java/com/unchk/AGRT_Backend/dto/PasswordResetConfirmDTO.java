package com.unchk.AGRT_Backend.dto;

import lombok.Data;

@Data
public class PasswordResetConfirmDTO {
    private String email;
    private String otpCode;
    private String newPassword;
}