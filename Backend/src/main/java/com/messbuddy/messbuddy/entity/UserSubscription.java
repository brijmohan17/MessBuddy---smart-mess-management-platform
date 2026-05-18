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
public class UserSubscription {
    @Id
    @JsonProperty("_id")
    private String id;

    private String userId;

    private String planId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String status; // Active|Expired|Cancelled|Pending|Plan Removed

    private String paymentId;

    private String paymentStatus; // Pending|Completed|Failed

    private String cancellationReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
