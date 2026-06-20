package com.example.MentalHealthAssistantApplication.service;

import com.example.MentalHealthAssistantApplication.dto.ApiResponse;
import com.example.MentalHealthAssistantApplication.dto.LoginRequest;
import com.example.MentalHealthAssistantApplication.dto.RegisterRequest;
import com.example.MentalHealthAssistantApplication.entity.User;
import com.example.MentalHealthAssistantApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for user registration and login.
 * Contains all business logic related to authentication.
 */
@Service
@RequiredArgsConstructor // Lombok: generates constructor for all final fields (used for DI)
public class UserService {

    // Injected via constructor (RequiredArgsConstructor handles this)
    private final UserRepository userRepository;

    /**
     * Registers a new user after checking for duplicate email.
     *
     * @param request - contains name, email, password
     * @return ApiResponse with success or failure message
     */
    public ApiResponse<User> register(RegisterRequest request) {

        // Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.failure("Email is already registered. Please use a different email.");
        }

        // Build new User entity from request DTO
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Plain text (no encoding for simplicity)

        // Save user to database
        User savedUser = userRepository.save(user);

        return ApiResponse.success("User registered successfully!", savedUser);
    }

    /**
     * Validates user credentials during login.
     *
     * @param request - contains email and password
     * @return ApiResponse with user data or error message
     */
    public ApiResponse<User> login(LoginRequest request) {

        // Try to find user by email
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        // If no user found with that email
        if (optionalUser.isEmpty()) {
            return ApiResponse.failure("No account found with this email address.");
        }

        User user = optionalUser.get();

        // Check if password matches
        if (!user.getPassword().equals(request.getPassword())) {
            return ApiResponse.failure("Incorrect password. Please try again.");
        }

        return ApiResponse.success("Login successful! Welcome back, " + user.getName(), user);
    }

    /**
     * Fetches a user by ID.
     * Used internally by other services (ChatService, MoodService).
     *
     * @param userId - the user's ID
     * @return Optional<User>
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}