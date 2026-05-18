package com.messbuddy.messbuddy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record FeedbackResponseDto(
        @JsonProperty("_id") String id,
        @JsonProperty("userID") UserSummary user,
        String comments,
        Integer rating,
        LocalDateTime submittedAt
) {
    public record UserSummary(
            @JsonProperty("_id") String id,
            String username
    ) {
    }
}