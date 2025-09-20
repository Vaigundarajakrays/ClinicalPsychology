package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.SubscribeDTO;
import com.clinicalpsychology.app.dto.SubscribeResponseDTO;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscribeController {

    private final SubscribeService subscribeService;

    @PostMapping
    public CommonResponse<SubscribeResponseDTO> subscribe(@RequestBody SubscribeDTO subscribeDTO) throws UnexpectedServerException, ResourceNotFoundException {
        return subscribeService.subscribe(subscribeDTO.getEmail());
    }

}
