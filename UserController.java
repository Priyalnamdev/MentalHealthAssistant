package com.example.MentalHealthAssistantApplication.controller;


import com.example.MentalHealthAssistantApplication.dto.ApiResponse;
import com.example.MentalHealthAssistantApplication.dto.LoginRequest;
import com.example.MentalHealthAssistantApplication.dto.RegisterRequest;
import com.example.MentalHealthAssistantApplication.entity.User;
import com.example.MentalHealthAssistantApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user authentication.
 * Handles registration and login endpoints.
 * Base URL: /api/users
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────
    // POST /api/users/register
    // Registers a new user account
    // ─────────────────────────────────────────────────────

    /**
     * Register a new user.
     *
     * Request Body:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "password": "pass123"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest request) {

        ApiResponse<User> response = userService.register(request);

        // Return 201 CREATED on success, 400 BAD REQUEST on failure
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ─────────────────────────────────────────────────────
    // POST /api/users/login
    // Validates user credentials and returns user info
    // ─────────────────────────────────────────────────────

    /**
     * Login with existing credentials.
     *
     * Request Body:
     * {
     *   "email": "john@example.com",
     *   "password": "pass123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody LoginRequest request) {

        ApiResponse<User> response = userService.login(request);

        // Return 200 OK on success, 401 UNAUTHORIZED on failure
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ─────────────────────────────────────────────────────
    // GET /api/users/{id}
    // Fetch a user's profile by their ID
    // ─────────────────────────────────────────────────────

    /**
     * Get user profile by ID.
     * Useful for displaying user info in the frontend.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {

        return userService.findById(id)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.success("User found!", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("User not found with ID: " + id)));
    }
}