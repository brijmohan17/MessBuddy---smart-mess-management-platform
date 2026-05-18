package com.messbuddy.messbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CheckIn {
    @Id
    @JsonProperty("_id")
    private String id;

    private String userId;

    private String messId;

    private String mealPassId;

    private String mealType; // breakfast|lunch|dinner

    private String status; // success|failed

    private String failureReason;

    private LocalDateTime createdAt;
}
