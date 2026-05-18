package com.messbuddy.messbuddy.entity;


import com.messbuddy.messbuddy.entity.type.LoginRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document()
public class User {
    @Id
    @JsonProperty("_id")
    private String id;

    @NotNull()
    @Indexed(unique = true)
    private String username;

    @NotNull
    @Field(name = "Login_Role")
    private LoginRole loginRole;

    @NotNull
    private String password;

    @NotNull
    @Indexed(unique = true)
    private String email;

    @Field(name = "UserID")
    private Long userId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public String getLoginRoleValue() {
        return loginRole == null ? null : loginRole.getValue();
    }

}
