package com.clinicalpsychology.app.aitherapist;

import com.clinicalpsychology.app.enums.Role;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.ClientProfile;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.payment.PaymentRequest;
import com.clinicalpsychology.app.payment.PaymentResponse;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.UsersRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.ProfanityCheckerService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Optional;


@RestController
@RequestMapping("/api/ai-therapist")
@RequiredArgsConstructor
@Slf4j
public class AiTherapistController {

    private final AiTherapistService aiTherapistService;
    private final ProfanityCheckerService profanityCheckerService;
    private final TempTokenService tempTokenService;
    private final UsersRepository usersRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final AiChatPaymentService aiChatPaymentService;

    // See sse-stream-api-authorised-case-study.md in docs package/directory for why this api is used
    @GetMapping("/sse-token")
    public CommonResponse<String> getSseToken() throws UnexpectedServerException {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return tempTokenService.generateToken(username);

    }


    // The spaces before each message is important
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAi(@RequestParam String message, @RequestParam String token) {

        if (!tempTokenService.isValid(token)) {
            return Flux.just(" Unauthorized. Invalid or expired token.");
        }

        String username = tempTokenService.getUsername(token);
        Optional<Users> userOpt = usersRepository.findByEmailId(username);

        if (userOpt.isEmpty()) {
            log.warn("No user found for token: {}", token);
            return Flux.just(" Unauthorized. User not found.");
        }

        Users currentUser = userOpt.get();

        if(currentUser.getRole() == Role.CLIENT){

            Optional<ClientProfile> clientOpt = clientProfileRepository.findByEmail(username);

            if (clientOpt.isEmpty()) {
                log.warn("No client profile found for username: {}", username);
                return Flux.just(" Unauthorized. Client profile not found.");
            }

            ClientProfile clientProfile = clientOpt.get();

            if(!clientProfile.isPaidForAiChat()){
                if(clientProfile.getChatCount() >= 10){
                    return Flux.just(" You have reached the free chat limit. Please upgrade your plan to continue");
                }

                // Increment the free usage
                clientProfile.setChatCount(clientProfile.getChatCount()+1);
                clientProfileRepository.save(clientProfile);
            }
        }

        if (message == null || message.length() > 500) {
            return Flux.just(" Message too long or invalid.");
        }

        if(profanityCheckerService.containsProfanity(message)){
            return Flux.just(" Your message violates our content policy. Please rephrase.");
        }

        return aiTherapistService.isFlaggedByModeration(message)
                .flatMapMany(isFlagged -> {
                    if (isFlagged) {
                        return Flux.just(" Your message violates our content policy. Please rephrase.");
                    } else {
                        return aiTherapistService.streamResponse(message);
                    }
                });
    }

    // trying
    @PostMapping("/checkout")
    public CommonResponse<PaymentResponse> checkoutProducts(@Valid @RequestBody AiChatPaymentDto aiChatPaymentDto) throws StripeException, UnexpectedServerException, ResourceNotFoundException {
        return aiChatPaymentService.checkoutProducts(aiChatPaymentDto);
    }
}

