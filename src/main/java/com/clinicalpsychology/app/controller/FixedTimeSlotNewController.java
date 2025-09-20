package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.TimeSlotDTO;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.FixedTimeSlotNewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FixedTimeSlotNewController {

    private final FixedTimeSlotNewService fixedTimeSlotNewService;

    //@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) means We are telling spring to convert the String into LocalDate with ISO format yyyy-mm-dd
    @GetMapping("/getTimeSlotsForTherapist")
    public CommonResponse<List<TimeSlotDTO>> getAllTimeSlotsOfTherapist(
            @RequestParam Long therapistId,
            @RequestParam Long clientId,
            @RequestParam String date) throws UnexpectedServerException, ResourceNotFoundException {
        return fixedTimeSlotNewService.getTimeSlotsOfTherapist(therapistId, clientId, date);
    }
}
