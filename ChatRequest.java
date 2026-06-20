package com.example.MentalHealthAssistantApplication.dto;


import lombok.Data;

@Data
public class ChatRequest {

    private Long userId;

    private String message;

}
