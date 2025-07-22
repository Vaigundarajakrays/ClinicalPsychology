package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exceptionHandling.InvalidFieldValueException;
import com.clinicalpsychology.app.exceptionHandling.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Feedback;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.FeedbackRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ClientProfileRepository clientProfileRepository;

    public CommonResponse<String> saveFeedback(Feedback feedback) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            if(feedback.getClientId()==null){
                throw new InvalidFieldValueException("Client id should not be null");
            }

            if(!clientProfileRepository.existsById(feedback.getClientId())){
                throw new ResourceNotFoundException("Client not found with id: " + feedback.getClientId());
            }

            if(feedbackRepository.existsByClientId(feedback.getClientId())){
                throw new ResourceAlreadyExistsException("You already gave feedback");
            }

            feedbackRepository.save(feedback);

            return CommonResponse.<String>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message("Thanks for your Feedback")
                    .build();

        } catch (ResourceNotFoundException | InvalidFieldValueException | ResourceAlreadyExistsException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException("Error while saving feedback: " + e.getMessage());
        }


    }
}
