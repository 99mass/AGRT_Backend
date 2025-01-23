package com.unchk.AGRT_Backend.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.unchk.AGRT_Backend.enums.UserRole;
import com.unchk.AGRT_Backend.utils.RoleDeserializer;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String profilePicture;
    @JsonDeserialize(using = RoleDeserializer.class)
    private UserRole role;
}