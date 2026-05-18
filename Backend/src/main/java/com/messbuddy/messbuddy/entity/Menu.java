package com.messbuddy.messbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Menu {
    @Id
    @JsonProperty("_id")
    private String id;

    @Field(name = "Menu_Name")
    private String Menu_Name;

    private String Description;

    private Double Price;

    @Field(name = "Owner_ID")
    private String Owner_ID; // user id

    private String Availability; // Yes/No

    @Field(name = "Food_Type")
    private String Food_Type; // Veg/Non-Veg

    private LocalDateTime Date;
}
