package com.codecool.poster.service;

import com.codecool.poster.model.Like;
import com.codecool.poster.model.media.Media;
import com.codecool.poster.model.Share;
import com.codecool.poster.model.post.Post;
import com.codecool.poster.model.post.SendPost;
import com.codecool.poster.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${media.service.url}")
    private String mediaUrl;

    @Value("${like.service.url}")
    private String likeUrl;

    @Value("${share.service.url}")
    private String shareUrl;

    private final int MAX_POST_MESSAGE_LENGTH = 250;

    public Collection<Post> findAll() {
        return postRepository.findAll();
    }

    public Collection<Post> findAllByPersonIdIn(Collection<Long> personId) { return postRepository.findAllByPersonIdIn(personId); }

    public Post findById(long id) {
        return postRepository.findById(id).orElse(null);
    }

    public boolean savePost(Post post) {
        if (!checkPostMessageLength(post.getMessage())) return false;
        post.setPostDate(LocalDateTime.now());
        postRepository.save(post);
        return true;
    }

    public Collection<SendPost> getPostsWithMedia(Collection<Post> posts) {
        Collection<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        HttpEntity<Collection<Long>> request = new HttpEntity<>(postIds);
        ResponseEntity<Media[]> mediaResponse = restTemplate.postForEntity(mediaUrl + "/medias-with-post", request, Media[].class);
        
        if (mediaResponse.getStatusCode().is2xxSuccessful()) {
            List<Media> media = Arrays.asList(Objects.requireNonNull(mediaResponse.getBody()));
            Collection<SendPost> result = new ArrayList<>();

            for (Post post: posts) {
                Collection<Media> tempMedia = media.stream().filter(m -> m.getPostId() == post.getId()).collect(Collectors.toList());
                result.add(new SendPost(post, tempMedia));
            }

            return result;
        }
        
        throw new IllegalArgumentException("Medias not found by post ids");
    }

    public Collection<Post> getPostsWithInteractions(Collection<Post> posts) {
        Collection<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        HttpEntity<Collection<Long>> request = new HttpEntity<>(postIds);
        ResponseEntity<Like[]> likeResponse = restTemplate.postForEntity(likeUrl + "/likes-with-post", request, Like[].class);
        ResponseEntity<Share[]> shareResponse = restTemplate.postForEntity(shareUrl + "/likes-with-post", request, Share[].class);

        if (likeResponse.getStatusCode().is2xxSuccessful() && shareResponse.getStatusCode().is2xxSuccessful()) {
            Collection<Like> likes = Arrays.asList(Objects.requireNonNull(likeResponse.getBody()));
            Collection<Share> shares = Arrays.asList(Objects.requireNonNull(shareResponse.getBody()));

            Collection<Long> likePostIds = likes.stream().map(Like::getPostId).collect(Collectors.toList());
            Collection<Long> sharePostIds = shares.stream().map(Share::getPostId).collect(Collectors.toList());

            Collection<Post> result = new ArrayList<>();

            for (Post post: posts) {
                long id = post.getId();
                post.setLiked(likePostIds.contains(id));
                post.setShared(sharePostIds.contains(id));
                result.add(post);
            }

            return result;
        }

        throw new IllegalArgumentException("Likes or Shares not found by post ids");
    }

    private boolean checkPostMessageLength(String message) { return message.length() <= MAX_POST_MESSAGE_LENGTH; }
}
