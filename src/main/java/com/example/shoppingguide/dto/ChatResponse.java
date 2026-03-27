package com.example.shoppingguide.dto;

import com.example.shoppingguide.domain.IntentType;

public record ChatResponse(
        IntentType intent,
        String response
) {}
