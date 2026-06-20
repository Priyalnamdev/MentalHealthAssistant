package com.example.MentalHealthAssistantApplication.dto;


import lombok.Data;

@Data
public class MoodRequest {

    private Long userId;

    private String mood;
}
