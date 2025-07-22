package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exceptionHandling.InvalidFieldValueException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.ContactMessage;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.ContactMessageRepository;
import com.clinicalpsychology.app.repository.TherapistProfileRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.clinicalpsychology.app.util.Constant.STATUS_TRUE;
import static com.clinicalpsychology.app.util.Constant.SUCCESS_CODE;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final ClientProfileRepository clientProfileRepository;

    public CommonResponse<String> sendMessage(ContactMessage contactMessage) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            if (contactMessage.getName() == null || contactMessage.getMessage() == null || contactMessage.getEmail() == null || contactMessage.getSubject() == null) {
                throw new InvalidFieldValueException("Name, email, subject, message should not be null");
            }

            if (contactMessage.getTherapistId() != null && !therapistProfileRepository.existsByIdAndEmail(contactMessage.getTherapistId(), contactMessage.getEmail())) {
                throw new ResourceNotFoundException(String.format("Therapist not found with email: %s or therapistId: %s", contactMessage.getEmail(), contactMessage.getTherapistId()));
            }

            if (contactMessage.getClientId() != null && !clientProfileRepository.existsByIdAndEmail(contactMessage.getClientId(), contactMessage.getEmail())) {
                throw new ResourceNotFoundException(String.format("Client not found with email: %s or clientId: %s", contactMessage.getEmail(), contactMessage.getClientId()));
            }

            contactMessageRepository.save(contactMessage);

            return CommonResponse.<String>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message("Successfully sent message")
                    .build();

        } catch (InvalidFieldValueException | ResourceNotFoundException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException("Error while sending message : " + e.getMessage());
        }

    }
}
