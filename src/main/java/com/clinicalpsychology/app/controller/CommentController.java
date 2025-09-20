package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.Comment;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService){
        this.commentService=commentService;
    }

    @PostMapping("/saveComment")
    public CommonResponse<Comment> addComment(@RequestBody Comment comment) throws UnexpectedServerException, ResourceNotFoundException {
        return commentService.addComment(comment);
    }

    @GetMapping("/getCommentsByPostId/{postId}")
    public CommonResponse<List<Comment>> getComments(@PathVariable Long postId) throws UnexpectedServerException, ResourceNotFoundException {
        return commentService.getCommentsByPostId(postId);
    }
}

