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
public class Prebooking {
    @Id
    @JsonProperty("_id")
    private String id;

    private String menuId;

    private String messId;

    private String userId;

    private String date;

    private String time;

    private Integer quantity;

    private String status; // Pending, Confirmed, Cancelled
}
