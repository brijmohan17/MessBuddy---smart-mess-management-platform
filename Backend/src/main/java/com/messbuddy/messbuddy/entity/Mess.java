package com.messbuddy.messbuddy.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder.Default;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Mess {

    @Id
    @JsonProperty("_id")
    private String id;

    @Field(name = "Mess_ID")
    private Long messId;
    private String Mess_Name;

    private String Mobile_No;

    private Integer Capacity;

    private String Address;

    @Field(name = "Owner_ID")
    private String Owner_ID; // User id reference

    private String Description;

    @Default
    private List<Integer> Ratings = new ArrayList<>();

    @Default
    private List<String> RatedBy = new ArrayList<>();

    @Field(name = "UserID")
    private Long UserID;

    @Default
    private String Image = "http://res.cloudinary.com/dq3ro4o3c/image/upload/v1734445757/gngcgm82wwo5t0desu0w.jpg";

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
