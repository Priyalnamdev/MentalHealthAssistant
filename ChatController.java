package com.example.MentalHealthAssistantApplication.controller;

import com.example.MentalHealthAssistantApplication.dto.ApiResponse;
import com.example.MentalHealthAssistantApplication.dto.ChatRequest;
import com.example.MentalHealthAssistantApplication.dto.ChatResponse;
import com.example.MentalHealthAssistantApplication.entity.ChatHistory;
import com.example.MentalHealthAssistantApplication.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for AI chatbot functionality.
 * Handles sending messages and retrieving chat history.
 * Base URL: /api/chat
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ─────────────────────────────────────────────────────
    // POST /api/chat/send
    // Sends a message to the AI and returns a response
    // ─────────────────────────────────────────────────────

    /**
     * Send a message to the AI mental health assistant.
     *
     * Request Body:
     * {
     *   "userId": 1,
     *   "message": "I am feeling very stressed today"
     * }
     *
     * Response includes:
     * - userMessage: original message
     * - aiResponse: mental health suggestion
     * - timestamp: time of conversation
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @RequestBody ChatRequest request) {

        ApiResponse<ChatResponse> response = chatService.chat(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ─────────────────────────────────────────────────────
    // GET /api/chat/history/{userId}
    // Fetches full chat history for a specific user
    // ─────────────────────────────────────────────────────

    /**
     * Get chat history for a user.
     * Returns all conversations sorted by timestamp (newest first).
     *
     * Example: GET /api/chat/history/1
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<ChatHistory>>> getChatHistory(
            @PathVariable Long userId) {

        ApiResponse<List<ChatHistory>> response = chatService.getChatHistory(userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}