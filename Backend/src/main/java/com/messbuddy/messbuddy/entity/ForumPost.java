package com.messbuddy.messbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ForumPost {
    @Id
    @JsonProperty("_id")
    private String id;

    private String title;

    private String content;

    private String author; // user id

    private String messId;

    private String type; // general, question, announcement, poll

    private List<String> tags = new ArrayList<>();

    private List<String> likes = new ArrayList<>();

    private List<Comment> comments = new ArrayList<>();

    private List<PollOption> pollOptions = new ArrayList<>();

    private Boolean isPollActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Comment {
        private String id;
        private String userId;
        private String content;
        private List<String> likes = new ArrayList<>();
        private LocalDateTime createdAt;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class PollOption {
        private String text;
        private List<String> votes = new ArrayList<>();
    }
}
