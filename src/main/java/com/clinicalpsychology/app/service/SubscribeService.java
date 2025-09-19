package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.dto.SubscribeResponseDTO;
import com.clinicalpsychology.app.enums.Role;
import com.clinicalpsychology.app.enums.SubscribeStatus;
import com.clinicalpsychology.app.exceptionHandling.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Subscribe;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.SubscribeRepository;
import com.clinicalpsychology.app.repository.TherapistProfileRepository;
import com.clinicalpsychology.app.repository.UsersRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final UsersRepository usersRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final ClientProfileRepository clientProfileRepository;

    public CommonResponse<SubscribeResponseDTO> subscribe(String email) throws ResourceNotFoundException, UnexpectedServerException {

        try {

            Users user = usersRepository.findByEmailId(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            // Double check
            if(user.getRole()== Role.THERAPIST){
                if(!therapistProfileRepository.existsByEmail(email)){
                    throw new ResourceNotFoundException("Therapist not found with email: " + email);
                }
            }

            if(user.getRole()==Role.CLIENT){
                if(!clientProfileRepository.existsByEmail(email)){
                    throw new ResourceNotFoundException("Client not found with email: " + email);
                }
            }

            var status = SubscribeResponseDTO.builder()
                    .isSubscribed(true)
                    .build();

            if(subscribeRepository.existsByEmail(email)){
                return CommonResponse.<SubscribeResponseDTO>builder()
                        .statusCode(SUCCESS_CODE)
                        .status(STATUS_TRUE)
                        .message("This user is already subscribed")
                        .data(status)
                        .build();
            }

            var subscribe = Subscribe.builder()
                    .email(email)
                    .role(user.getRole())
                    .status(SubscribeStatus.SUBSCRIBED)
                    .build();

            subscribeRepository.save(subscribe);

            return CommonResponse.<SubscribeResponseDTO>builder()
                    .statusCode(SUCCESS_CODE)
                    .status(STATUS_TRUE)
                    .message("Subscribed successfully")
                    .data(status)
                    .build();

        } catch (ResourceNotFoundException | ResourceAlreadyExistsException e){
            throw e;
        } catch (Exception e){
            throw new UnexpectedServerException("Error while subscribing: " + e.getMessage());
        }

    }
}
