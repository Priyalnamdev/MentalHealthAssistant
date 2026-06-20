package com.example.MentalHealthAssistantApplication.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private String userMessage;

    private String aiResponse;

    private String timestamp;
}
