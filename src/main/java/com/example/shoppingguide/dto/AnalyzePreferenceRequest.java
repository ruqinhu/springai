package com.example.shoppingguide.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyzePreferenceRequest(
        @NotBlank(message = "Description cannot be empty")
        String description
) {}
