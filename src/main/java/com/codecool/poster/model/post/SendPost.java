package com.codecool.poster.model.post;

import com.codecool.poster.model.media.Media;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendPost {

    private Post post;

    private Collection<Media> media;
}
