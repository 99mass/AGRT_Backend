package com.unchk.AGRT_Backend.dto;

import com.unchk.AGRT_Backend.enums.UserRole;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private UserRole role;
}