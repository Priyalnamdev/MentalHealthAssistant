package com.example.MentalHealthAssistantApplication.controller;


import com.example.MentalHealthAssistantApplication.dto.ApiResponse;
import com.example.MentalHealthAssistantApplication.dto.MoodRequest;
import com.example.MentalHealthAssistantApplication.entity.Mood;
import com.example.MentalHealthAssistantApplication.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for mood tracking.
 * Handles saving and retrieving mood entries.
 * Base URL: /api/mood
 */
@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    // ─────────────────────────────────────────────────────
    // POST /api/mood/save
    // Logs a new mood entry for a user
    // ─────────────────────────────────────────────────────

    /**
     * Save a mood entry for the user.
     *
     * Request Body:
     * {
     *   "userId": 1,
     *   "mood": "happy"
     * }
     *
     * Accepted mood values (not restricted):
     * happy, sad, stressed, anxious, calm, angry, lonely, etc.
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Mood>> saveMood(@RequestBody MoodRequest request) {

        ApiResponse<Mood> response = moodService.saveMood(request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ─────────────────────────────────────────────────────
    // GET /api/mood/history/{userId}
    // Fetches mood history for a specific user
    // ─────────────────────────────────────────────────────

    /**
     * Get mood history for a user.
     * Returns list of moods sorted by date (newest first).
     *
     * Example: GET /api/mood/history/1
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<Mood>>> getMoodHistory(@PathVariable Long userId) {

        ApiResponse<List<Mood>> response = moodService.getMoodHistory(userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}