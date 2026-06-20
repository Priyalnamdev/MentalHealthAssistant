package com.example.MentalHealthAssistantApplication.service;

import com.example.MentalHealthAssistantApplication.dto.ApiResponse;
import com.example.MentalHealthAssistantApplication.dto.ChatRequest;
import com.example.MentalHealthAssistantApplication.dto.ChatResponse;
import com.example.MentalHealthAssistantApplication.entity.ChatHistory;
import com.example.MentalHealthAssistantApplication.entity.User;
import com.example.MentalHealthAssistantApplication.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.Optional;

/**
 * Service class for AI chatbot functionality.
 * Supports:
 *   - Mock responses (when ai.mock.enabled=true)
 *   - Real OpenAI API calls (when configured)
 *   - Real Gemini API calls (when configured)
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final UserService userService;
    private final RestTemplate restTemplate; // For making HTTP calls to AI APIs

    // Read values from application.properties
    @Value("${ai.mock.enabled:true}")
    private boolean mockEnabled;

    @Value("${ai.provider:mock}")
    private String aiProvider;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.api-url:}")
    private String openAiApiUrl;

    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String openAiModel;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.api-url:}")
    private String geminiApiUrl;

    /**
     * Main method: processes user message and returns AI response.
     * Automatically picks mock or real AI based on configuration.
     *
     * @param request - contains userId and message
     * @return ApiResponse with ChatResponse (message + AI reply)
     */
    public ApiResponse<ChatResponse> chat(ChatRequest request) {

        // Validate user exists
        Optional<User> optionalUser = userService.findById(request.getUserId());
        if (optionalUser.isEmpty()) {
            return ApiResponse.failure("User not found with ID: " + request.getUserId());
        }

        // Validate message is not empty
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.failure("Message cannot be empty.");
        }

        User user = optionalUser.get();
        String userMessage = request.getMessage().trim();
        String aiReply;

        // ─── Route to mock or real AI ───────────────────────────
        if (mockEnabled) {
            aiReply = getMockResponse(userMessage);         // Use mock
        } else if ("openai".equalsIgnoreCase(aiProvider)) {
            aiReply = callOpenAI(userMessage);              // Use OpenAI
        } else if ("gemini".equalsIgnoreCase(aiProvider)) {
            aiReply = callGemini(userMessage);              // Use Gemini
        } else {
            aiReply = getMockResponse(userMessage);         // Fallback to mock
        }

        // ─── Save conversation to database ───────────────────────
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUser(user);
        chatHistory.setMessage(userMessage);
        chatHistory.setResponse(aiReply);
        // timestamp auto-set by @PrePersist

        chatHistoryRepository.save(chatHistory);

        // ─── Build and return response ────────────────────────────
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ChatResponse chatResponse = new ChatResponse(userMessage, aiReply, timestamp);

        return ApiResponse.success("Response generated successfully!", chatResponse);
    }

    /**
     * Fetches full chat history for a user, newest first.
     *
     * @param userId - the user's ID
     * @return ApiResponse with list of ChatHistory entries
     */
    public ApiResponse<List<ChatHistory>> getChatHistory(Long userId) {

        Optional<User> optionalUser = userService.findById(userId);
        if (optionalUser.isEmpty()) {
            return ApiResponse.failure("User not found with ID: " + userId);
        }

        List<ChatHistory> history =
                chatHistoryRepository.findByUserOrderByTimestampDesc(optionalUser.get());

        if (history.isEmpty()) {
            return ApiResponse.success("No chat history found for this user.", history);
        }

        return ApiResponse.success("Chat history fetched successfully!", history);
    }

    // ════════════════════════════════════════════════════════════
    // MOCK RESPONSE LOGIC
    // Returns pre-written mental health suggestions based on keywords
    // ════════════════════════════════════════════════════════════

    /**
     * Returns a meaningful mock mental health response
     * based on keywords found in the user's message.
     *
     * @param message - user's input message
     * @return pre-written mental health suggestion
     */
    private String getMockResponse(String message) {

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("anxious") || lowerMessage.contains("anxiety")) {
            return "I understand you're feeling anxious. Try this: Take 5 slow deep breaths — " +
                    "inhale for 4 seconds, hold for 4, exhale for 4. This activates your body's " +
                    "calming response. Remember, anxiety is temporary and you have the strength to get through it. 💙";
        }

        if (lowerMessage.contains("stress") || lowerMessage.contains("stressed")) {
            return "Stress can feel overwhelming. Here are 3 quick tips: " +
                    "1) Take a 5-minute walk outside. " +
                    "2) Write down your top 3 priorities for today. " +
                    "3) Drink a glass of water and take 3 deep breaths. " +
                    "You're doing better than you think! 🌿";
        }

        if (lowerMessage.contains("sad") || lowerMessage.contains("depressed") ||
                lowerMessage.contains("unhappy")) {
            return "I'm sorry you're feeling this way. It's okay to feel sad sometimes — " +
                    "your feelings are valid. Try reaching out to someone you trust, " +
                    "or engage in a small activity you enjoy. " +
                    "If these feelings persist, please consider speaking with a mental health professional. 💛";
        }

        if (lowerMessage.contains("happy") || lowerMessage.contains("great") ||
                lowerMessage.contains("good")) {
            return "That's wonderful to hear! 😊 Keep nurturing that positivity. " +
                    "Consider journaling what made you feel good today — " +
                    "it helps reinforce positive emotions and can lift your mood on harder days.";
        }

        if (lowerMessage.contains("sleep") || lowerMessage.contains("insomnia") ||
                lowerMessage.contains("tired")) {
            return "Poor sleep seriously affects mental health. Try these tips tonight: " +
                    "1) Avoid screens 30 mins before bed. " +
                    "2) Keep your room cool and dark. " +
                    "3) Try a body scan meditation — tense and release each muscle group. " +
                    "Good sleep is the foundation of good mental health. 🌙";
        }

        if (lowerMessage.contains("lonely") || lowerMessage.contains("alone")) {
            return "Feeling lonely is more common than you might think, " +
                    "and it takes courage to acknowledge it. " +
                    "Try joining an online community around a hobby you enjoy, " +
                    "or reach out to one person today — even a simple 'hey' can start a connection. " +
                    "You matter and your presence is valued. 🤝";
        }

        if (lowerMessage.contains("angry") || lowerMessage.contains("frustrated")) {
            return "It's completely natural to feel angry or frustrated. " +
                    "Try this: Step away from the situation for 10 minutes, " +
                    "do some physical activity like jumping jacks or a brisk walk, " +
                    "then revisit the situation with fresh eyes. " +
                    "Anger is energy — redirect it constructively. 🔥➡️💡";
        }

        if (lowerMessage.contains("help") || lowerMessage.contains("support")) {
            return "Reaching out for help is a sign of strength, not weakness. 💪 " +
                    "I'm here to support you. You can talk to me about how you're feeling, " +
                    "track your mood daily, and build healthy mental habits. " +
                    "If you need professional support, please consider contacting a " +
                    "licensed therapist or calling a mental health helpline in your area.";
        }

        if (lowerMessage.contains("meditat") || lowerMessage.contains("calm") ||
                lowerMessage.contains("relax")) {
            return "Meditation is a powerful tool for mental wellness. 🧘 " +
                    "Start with just 5 minutes: sit comfortably, close your eyes, " +
                    "focus on your breath, and gently bring your mind back when it wanders. " +
                    "Apps like Headspace or Insight Timer can guide you. " +
                    "Even 5 minutes a day can create lasting change.";
        }

        // Default response for unrecognized input
        return "Thank you for sharing with me. 🌱 " +
                "Mental wellness is a journey, not a destination. " +
                "Some helpful daily habits include: staying hydrated, getting 7-8 hours of sleep, " +
                "moving your body for at least 20 minutes, and connecting with someone you care about. " +
                "I'm here whenever you need to talk. What else is on your mind?";
    }

    // ════════════════════════════════════════════════════════════
    // OPENAI API INTEGRATION
    // Uncomment ai.provider=openai in application.properties to use
    // ════════════════════════════════════════════════════════════

    /**
     * Calls the OpenAI Chat Completions API.
     * Uses GPT model to generate mental health suggestions.
     *
     * @param userMessage - the user's input
     * @return AI-generated response string
     */
    private String callOpenAI(String userMessage) {
        try {
            // Set request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey); // Authorization: Bearer sk-...

            // Build system prompt for mental health context
            String systemPrompt = "You are a compassionate and supportive mental health assistant. " +
                    "Provide helpful, empathetic, and practical mental health suggestions. " +
                    "Always recommend professional help for serious concerns. " +
                    "Keep responses concise, warm, and actionable.";

            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "model", openAiModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make POST call to OpenAI
            ResponseEntity<Map> responseEntity =
                    restTemplate.postForEntity(openAiApiUrl, entity, Map.class);

            // Parse response
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) responseEntity.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> messageObj =
                            (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageObj.get("content");
                }
            }
        } catch (Exception e) {
            // If OpenAI call fails, fall back to mock response
            System.err.println("OpenAI API call failed: " + e.getMessage());
        }

        return getMockResponse(userMessage); // Fallback
    }

    // ════════════════════════════════════════════════════════════
    // GEMINI API INTEGRATION
    // Uncomment ai.provider=gemini in application.properties to use
    // ════════════════════════════════════════════════════════════

    /**
     * Calls the Google Gemini API to generate mental health suggestions.
     *
     * @param userMessage - the user's input
     * @return AI-generated response string
     */
    private String callGemini(String userMessage) {
        try {
            // Set request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build mental health context prompt
            String fullPrompt = "You are a compassionate mental health assistant. " +
                    "Provide helpful and empathetic mental health suggestions for: " + userMessage;

            // Build Gemini request body structure
            Map<String, Object> textPart   = Map.of("text", fullPrompt);
            Map<String, Object> parts      = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(parts));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Append API key as query param (Gemini style)
            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

            // Make POST call to Gemini
            ResponseEntity<Map> responseEntity =
                    restTemplate.postForEntity(urlWithKey, entity, Map.class);

            // Parse Gemini response
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) responseEntity.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content =
                            (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts2 =
                            (List<Map<String, Object>>) content.get("parts");
                    if (parts2 != null && !parts2.isEmpty()) {
                        return (String) parts2.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            // If Gemini call fails, fall back to mock response
            System.err.println("Gemini API call failed: " + e.getMessage());
        }

        return getMockResponse(userMessage); // Fallback
    }
}