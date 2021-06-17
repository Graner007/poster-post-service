package com.codecool.poster.controller;

import com.codecool.poster.model.Follow;
import com.codecool.poster.model.Media;
import com.codecool.poster.model.MediaTypeEnum;
import com.codecool.poster.model.post.Post;
import com.codecool.poster.model.post.SendPost;
import com.codecool.poster.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${follow.service.url}")
    private String followUrl;

    @Value("${media.service.url}")
    private String mediaUrl;

    @GetMapping
    public ResponseEntity getAllPostsPersonalized(@RequestHeader long personId) {
        ResponseEntity<Follow[]> followResponse = restTemplate.postForEntity(followUrl + "/followers", personId, Follow[].class);

        if (followResponse.getStatusCode().is2xxSuccessful()) {
            List<Follow> follows = Arrays.asList(Objects.requireNonNull(followResponse.getBody()));

            Collection<Post> posts = postService.findAllByPersonIdIn(follows
                    .stream()
                    .map(Follow::getFollowedPersonId)
                    .collect(Collectors.toList()));

            Collection<Post> interactionPosts = postService.getPostsWithInteractions(posts);

            Collection<SendPost> mediaPosts = postService.getPostsWithMedia(interactionPosts);

            Map<Object, Object> postsMap = new HashMap<>();
            postsMap.put("posts", mediaPosts);

            return ResponseEntity.ok(postsMap);
        }

        throw new IllegalArgumentException("Person id does not exist!");
    }

    @GetMapping("/{id}")
    public Post getPostWithId(@PathVariable long id) {
        return postService.findById(id);
    }

    @PostMapping(value = "/add")
    public boolean savePost(@RequestHeader long personId,
                            @RequestParam String message,
                            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        boolean hasImage = false;
        boolean hasVideo = false;
        List<Media> media = new ArrayList<>(4);
        Post.PostBuilder postBuilder = Post
                .builder()
                .message(message)
                .personId(personId);

        Post finalPost = postBuilder.build();

        if (!(files == null)) {
            for (MultipartFile file: files) {
                switch (file.getContentType()) {
                    case "image/png":
                        hasImage = true;
                        break;
                    case "image/gif":
                        hasImage = true;
                        break;
                    case "image/jpeg":
                        hasImage = true;
                        break;
                    case "image/jpg":
                        hasImage = true;
                        break;
                    case "video/mp4":
                        hasVideo = true;
                        break;
                }
                if ((hasImage && hasVideo) || (!hasImage && !hasVideo) || (hasVideo && files.length > 1) || (hasImage && files.length > 4)) return false;
            }
            finalPost = postBuilder
                    .hasImage(hasImage)
                    .hasVideo(hasVideo)
                    .imageCount(hasImage ? files.length : 0)
                    .build();

            MediaTypeEnum mediaType = MediaTypeEnum.IMAGE;
            if (hasVideo) mediaType = MediaTypeEnum.VIDEO;

            HttpEntity<MultipartFile[]> request = new HttpEntity<>(files);
            ResponseEntity<String[]> mediaResponse = restTemplate.postForEntity(mediaUrl + "/submit", request, String[].class);

            if (mediaResponse.getStatusCode().is2xxSuccessful()) {
                String[] routes = mediaResponse.getBody();

                for (String route: routes) {
                    Media tempMedia = Media.builder()
                            .postId(finalPost.getId())
                            .mediaRoute(route)
                            .mediaType(mediaType)
                            .build();
                    media.add(tempMedia);
                }
            }
        }

        postService.savePost(finalPost);
        HttpEntity<List<Media>> request = new HttpEntity<>(media);
        restTemplate.postForEntity(mediaUrl + "/save-all", request, HttpEntity.class);
        return true;
    }
}
