package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Comment;
import com.clinicalpsychology.app.repository.CommentRepository;
import com.clinicalpsychology.app.repository.CommunityPostRepository;
import com.clinicalpsychology.app.repository.UsersRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UsersRepository usersRepository;
    private final CommunityPostRepository communityPostRepository;

    public CommentService(CommentRepository commentRepository, UsersRepository usersRepository, CommunityPostRepository communityPostRepository){
        this.commentRepository=commentRepository;
        this.usersRepository=usersRepository;
        this.communityPostRepository=communityPostRepository;
    }

    public CommonResponse<Comment> addComment(Comment comment) throws ResourceNotFoundException, UnexpectedServerException {

        boolean isUserExist = usersRepository.existsById(comment.getUserId());
        if (!isUserExist) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + comment.getUserId());
        }

        boolean isPostExist = communityPostRepository.existsById(comment.getPostId());
        if (!isPostExist) {
            throw new ResourceNotFoundException(POST_NOT_FOUND_WITH_ID + comment.getPostId());
        }

        try {
            Comment savedComment = commentRepository.save(comment);

            return CommonResponse.<Comment>builder()
                    .message(SUCCESSFULLY_ADDED)
                    .status(STATUS_TRUE)
                    .data(savedComment)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_SAVING_COMMENT + e.getMessage());
        }
    }

    public CommonResponse<List<Comment>> getCommentsByPostId(Long postId) throws ResourceNotFoundException, UnexpectedServerException {

        boolean isPostExist = communityPostRepository.existsById(postId);
        if (!isPostExist) {
            throw new ResourceNotFoundException(POST_NOT_FOUND_WITH_ID + postId);
        }

        try {
            List<Comment> comments = commentRepository.findByPostId(postId);

            if (comments.isEmpty()) {
                return CommonResponse.<List<Comment>>builder()
                        .message(NO_COMMENTS)
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .data(comments)
                        .build();
            }

            return CommonResponse.<List<Comment>>builder()
                    .message(LOADED_COMMENTS)
                    .status(STATUS_TRUE)
                    .data(comments)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_FETCHING_COMMENTS + e.getMessage());
        }
    }
}
