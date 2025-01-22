package com.unchk.AGRT_Backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unchk.AGRT_Backend.enums.UserRole;
import com.unchk.AGRT_Backend.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Rechercher un utilisateur par email
    Optional<User> findByEmail(String email);
    
    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);
    
    // Rechercher des utilisateurs par nom
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    
    // Rechercher des utilisateurs par prénom
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    
    // Rechercher des utilisateurs par rôle
    List<User> findByRole(UserRole role);
}