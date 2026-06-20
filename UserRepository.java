package com.example.MentalHealthAssistantApplication.repository;

import com.example.MentalHealthAssistantApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used during login to validate credentials.
     * Spring Data JPA auto-generates the query from method name.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user already exists with the given email.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);

}
