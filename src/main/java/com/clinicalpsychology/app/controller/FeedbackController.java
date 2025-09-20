package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.Feedback;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public CommonResponse<String> saveFeedback(@RequestBody Feedback feedback) throws UnexpectedServerException, ResourceNotFoundException {
        return feedbackService.saveFeedback(feedback);
    }
}
