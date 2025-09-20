package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.CommunityPost;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.CommunityPostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService){
        this.communityPostService=communityPostService;
    }

    @PostMapping("/savePosts")
    public CommonResponse<CommunityPost> createPost(@RequestBody CommunityPost post) throws UnexpectedServerException, ResourceNotFoundException {
        return communityPostService.createPost(post);
    }

    @GetMapping("/getAllPosts")
    public CommonResponse<List<CommunityPost>> getAllPosts() throws UnexpectedServerException {
        return communityPostService.getAllPosts();
    }

    @GetMapping("/getPostById/{id}")
    public CommonResponse<CommunityPost> getPostById(@PathVariable Long id) throws ResourceNotFoundException {
        return communityPostService.getPostById(id);
    }

    @DeleteMapping("/deletePostById/{id}")
    public CommonResponse<Void> deletePost(@PathVariable Long id) throws ResourceNotFoundException, UnexpectedServerException {
        return communityPostService.deletePost(id);
    }
}

