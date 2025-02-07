package com.unchk.AGRT_Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.unchk.AGRT_Backend.enums.UserRole;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DefaultAdminConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email:admin@admin.com}")
    private String defaultAdminEmail;

    @Value("${admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Value("${admin.default.firstName:Admin}")
    private String defaultAdminFirstName;

    @Value("${admin.default.lastName:Default}")
    private String defaultAdminLastName;

    @Bean
    public CommandLineRunner initializeDefaultAdmin() {
        return args -> {
            if (!userRepository.existsByEmail(defaultAdminEmail)) {
                User admin = new User();
                admin.setEmail(defaultAdminEmail);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setFirstName(defaultAdminFirstName);
                admin.setLastName(defaultAdminLastName);
                admin.setRole(UserRole.ADMIN);

                userRepository.save(admin);

                System.out.println("Default admin account created successfully");
            } else {
                System.out.println("Default admin account already exists");
            }
        };
    }
}