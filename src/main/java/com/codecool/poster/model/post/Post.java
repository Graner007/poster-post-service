package com.codecool.poster.model.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long personId;

    private String message;

    private boolean hasImage;

    private boolean hasVideo;

    private LocalDateTime postDate;

    private int adomCount;

    private int commentCount;

    private int shareCount;

    private int imageCount;

    @Transient
    private boolean isLiked;

    @Transient
    private boolean isShared;
}