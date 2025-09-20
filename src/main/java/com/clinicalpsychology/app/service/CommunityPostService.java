package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exception.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.CommunityPost;
import com.clinicalpsychology.app.repository.CommunityPostRepository;
import com.clinicalpsychology.app.repository.UsersRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final UsersRepository usersRepository;

    public CommunityPostService(CommunityPostRepository communityPostRepository, UsersRepository usersRepository){
        this.communityPostRepository=communityPostRepository;
        this.usersRepository=usersRepository;
    }

    public CommonResponse<CommunityPost> createPost(CommunityPost post) throws ResourceNotFoundException, UnexpectedServerException {

        boolean isPresent = usersRepository.existsById(post.getUserId());

        if (!isPresent) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + post.getUserId());
        }

        boolean isExists = communityPostRepository.existsByUserIdAndTitle(post.getUserId(), post.getTitle());

        if (isExists) {
            throw new ResourceAlreadyExistsException(TITLE_ALREADY_EXISTS);
        }

        try {
            CommunityPost savedPost = communityPostRepository.save(post);

            return CommonResponse.<CommunityPost>builder()
                    .message(SUCCESSFULLY_ADDED)
                    .status(STATUS_TRUE)
                    .data(savedPost)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_SAVING_POST + e.getMessage());
        }
    }

    public CommonResponse<List<CommunityPost>> getAllPosts() throws UnexpectedServerException {

        try {
            List<CommunityPost> posts = communityPostRepository.findAll();

            if (posts.isEmpty()) {
                return CommonResponse.<List<CommunityPost>>builder()
                        .status(STATUS_FALSE)
                        .message(NO_POSTS_AVAILABLE)
                        .data(posts)
                        .statusCode(SUCCESS_CODE)
                        .build();
            }

            return CommonResponse.<List<CommunityPost>>builder()
                    .status(STATUS_TRUE)
                    .message(LOADED_COMMUNITY_POSTS)
                    .data(posts)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_FETCHING_POST + e.getMessage());
        }
    }

    public CommonResponse<CommunityPost> getPostById(Long id) throws ResourceNotFoundException {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(POST_NOT_FOUND_WITH_ID + id));

        return CommonResponse.<CommunityPost>builder()
                .message(LOADED_COMMUNITY_POSTS)
                .status(STATUS_TRUE)
                .statusCode(SUCCESS_CODE)
                .data(post)
                .build();
    }

    public CommonResponse<Void> deletePost(Long id) throws ResourceNotFoundException, UnexpectedServerException {
        if (!communityPostRepository.existsById(id)) {
            throw new ResourceNotFoundException(POST_NOT_FOUND_WITH_ID + id);
        }

        try {

            communityPostRepository.deleteById(id);

            return CommonResponse.<Void>builder()
                    .message(POSTS_DELETED_SUCCESSFULLY)
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException("Error while deleting posts: " + e.getMessage());
        }
    }




}
