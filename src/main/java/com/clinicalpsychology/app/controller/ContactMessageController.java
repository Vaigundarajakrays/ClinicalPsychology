package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.ContactMessage;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public CommonResponse<String> sendMessage(@RequestBody ContactMessage contactMessage) throws UnexpectedServerException, ResourceNotFoundException {
        return contactMessageService.sendMessage(contactMessage);
    }
}
