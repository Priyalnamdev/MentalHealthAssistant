package com.example.MentalHealthAssistantApplication.service;


import com.example.MentalHealthAssistantApplication.dto.ApiResponse;
import com.example.MentalHealthAssistantApplication.dto.MoodRequest;
import com.example.MentalHealthAssistantApplication.entity.Mood;
import com.example.MentalHealthAssistantApplication.entity.User;
import com.example.MentalHealthAssistantApplication.repository.MoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for mood tracking functionality.
 * Handles saving and retrieving user mood entries.
 */
@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodRepository moodRepository;
    private final UserService userService; // Reuse UserService to find users

    /**
     * Saves a mood entry for the given user.
     *
     * @param request - contains userId and mood string
     * @return ApiResponse with saved mood or error
     */
    public ApiResponse<Mood> saveMood(MoodRequest request) {

        // Validate that user exists
        Optional<User> optionalUser = userService.findById(request.getUserId());

        if (optionalUser.isEmpty()) {
            return ApiResponse.failure("User not found with ID: " + request.getUserId());
        }

        // Validate mood value is not empty
        if (request.getMood() == null || request.getMood().trim().isEmpty()) {
            return ApiResponse.failure("Mood value cannot be empty.");
        }

        // Build mood entry
        Mood mood = new Mood();
        mood.setUser(optionalUser.get());
        mood.setMood(request.getMood().toLowerCase().trim()); // Normalize to lowercase

        // Save to database (date auto-set by @PrePersist)
        Mood savedMood = moodRepository.save(mood);

        return ApiResponse.success("Mood logged successfully!", savedMood);
    }

    /**
     * Fetches full mood history for a user, newest first.
     *
     * @param userId - the user's ID
     * @return ApiResponse with list of mood entries
     */
    public ApiResponse<List<Mood>> getMoodHistory(Long userId) {

        // Validate that user exists
        Optional<User> optionalUser = userService.findById(userId);

        if (optionalUser.isEmpty()) {
            return ApiResponse.failure("User not found with ID: " + userId);
        }

        // Fetch mood history sorted by date descending
        List<Mood> moods = moodRepository.findByUserOrderByDateDesc(optionalUser.get());

        if (moods.isEmpty()) {
            return ApiResponse.success("No mood entries found for this user.", moods);
        }

        return ApiResponse.success("Mood history fetched successfully!", moods);
    }
}