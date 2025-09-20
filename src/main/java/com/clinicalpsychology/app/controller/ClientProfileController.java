package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.ClientDashboardDTO;
import com.clinicalpsychology.app.dto.ClientProfileDTO;
import com.clinicalpsychology.app.dto.RescheduleDTO;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.ClientProfile;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.ClientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    @PostMapping("/register")
    public CommonResponse<ClientProfile> signUp(@RequestBody ClientProfileDTO clientProfileDTO) throws UnexpectedServerException {
        return clientProfileService.registerClient(clientProfileDTO);
    }

    @GetMapping("/getProfileDetails/{id}")
    public CommonResponse<ClientProfileDTO> getProfileDetails(@PathVariable Long id) throws UnexpectedServerException, ResourceNotFoundException {
        return clientProfileService.getClientProfile(id);
    }

    @PatchMapping("/updateProfile/{id}")
    public CommonResponse<ClientProfileDTO> updateProfile(@PathVariable Long id, @RequestBody ClientProfileDTO clientProfileDTO) throws UnexpectedServerException, ResourceNotFoundException {
        return clientProfileService.updateClientProfile(id, clientProfileDTO);
    }

    @GetMapping("/getAppointments/{clientId}")
    public CommonResponse<List<ClientDashboardDTO>> getAppointments(@PathVariable Long clientId) throws ResourceNotFoundException, UnexpectedServerException {
        return clientProfileService.getAppointments(clientId);
    }

    @PatchMapping("/bookings/{bookingId}/reschedule")
    public CommonResponse<ClientDashboardDTO> rescheduleBooking(@PathVariable Long bookingId, @RequestBody RescheduleDTO rescheduleDTO) throws ResourceNotFoundException, UnexpectedServerException {
        return clientProfileService.rescheduleBooking(bookingId, rescheduleDTO);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public CommonResponse<String> cancelBooking(@PathVariable Long bookingId) throws UnexpectedServerException, ResourceNotFoundException {
        return clientProfileService.cancelBooking(bookingId);
    }
}
