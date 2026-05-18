package com.messbuddy.messbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPlan {
    @Id
    @JsonProperty("_id")
    private String id;

    @Field(name = "messId")
    private String messId; // reference to Mess owner id

    private String planName;

    private String duration; // Daily|Weekly|Monthly

    private String mealType; // Veg|Non-Veg|Jain

    private Double price;

    private String description;

    private Boolean isActive = true;
}
