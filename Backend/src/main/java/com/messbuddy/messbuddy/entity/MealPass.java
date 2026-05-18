package com.messbuddy.messbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MealPass {
    @Id
    @JsonProperty("_id")
    private String id;

    private String userId;

    private String subscriptionId;

    private String messId;

    private String qrCode;

    private Boolean isActive = true;

    private Boolean isBlocked = false;

    private String blockReason;

    private LocalDateTime validFrom;

    private LocalDateTime validTill;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
