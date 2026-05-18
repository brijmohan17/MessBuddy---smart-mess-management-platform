package com.messbuddy.messbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserFeedback {
    @Id
    private String id;

    private String userID;

    private String comments;

    private Integer rating;

    private LocalDateTime submittedAt;
}
