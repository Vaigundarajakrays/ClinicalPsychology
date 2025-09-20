package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.*;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController{

    private final AdminService adminService;

    @GetMapping("/getDashboardDetails")
    public CommonResponse<AdminDashboardDTO> getAdminDashboardDetails() throws UnexpectedServerException {
        return adminService.getAdminDashboardDetails();
    }

    @GetMapping("/getAllTherapistSessions")
    public CommonResponse<List<TherapistAppointmentsDTO>> getAllTherapistSessions() throws UnexpectedServerException, ResourceNotFoundException {
        return adminService.getAllTherapistSessions();
    }

    @GetMapping("/getAllClientSessions")
    public CommonResponse<List<ClientAppointmentsDTO>> getAllClientSessions() throws UnexpectedServerException, ResourceNotFoundException {
        return adminService.getAllClientSessions();
    }

    @GetMapping("/therapists/overview")
    public CommonResponse<List<TherapistOverviewDTO>> getTherapistsOverview() throws UnexpectedServerException {
        return adminService.getTherapistsOverview();
    }

    @GetMapping("/clients/overview")
    public CommonResponse<List<ClientOverviewDTO>> getClientsOverview() throws UnexpectedServerException {
        return adminService.getClientsOverview();
    }

    @PatchMapping("/therapists/{therapistId}/approval-status")
    public CommonResponse<AdminDashboardDTO> updateTherapistStatus(@PathVariable Long therapistId,@RequestBody ApprovalRequestDTO request) throws UnexpectedServerException, ResourceNotFoundException {
        return adminService.updateTherapistApprovalStatus(therapistId, request);

    }
}

