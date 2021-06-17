package com.codecool.poster.model.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Media {

    private long postId;

    private String mediaRoute;

    private MediaTypeEnum mediaType;
}
