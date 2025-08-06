package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.AllTherapistsResponseDTO;
import com.clinicalpsychology.app.dto.TherapistDashboardDTO;
import com.clinicalpsychology.app.dto.TherapistProfileDTO;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.TherapistProfile;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.TherapistProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist")
@RequiredArgsConstructor
public class TherapistProfileController {

    private final TherapistProfileService therapistProfileService;

    @PostMapping("/register")
    public CommonResponse<String> registerNewTherapist(@RequestBody TherapistProfileDTO therapistProfileDTO) throws UnexpectedServerException {
        return therapistProfileService.registerTherapist(therapistProfileDTO);
    }

    @GetMapping("/getProfileDetails/{id}")
    public CommonResponse<TherapistProfileDTO> getProfileDetails(@PathVariable Long id) throws UnexpectedServerException, ResourceNotFoundException {
        return therapistProfileService.getProfileDetails(id);
    }

    @PatchMapping("/updateProfile/{id}")
    public CommonResponse<TherapistProfileDTO> updateProfile(@PathVariable Long id, @RequestBody TherapistProfileDTO therapistProfileDTO) throws UnexpectedServerException, ResourceNotFoundException {
        return therapistProfileService.updateTherapistProfile(id, therapistProfileDTO);
    }

    @GetMapping("/getAppointments/{therapistId}")
    public CommonResponse<List<TherapistDashboardDTO>> getAppointments(@PathVariable Long therapistId) throws ResourceNotFoundException, UnexpectedServerException {
        return therapistProfileService.getAppointments(therapistId);
    }

//    @GetMapping("/search")
//    public CommonResponse<List<TherapistProfileDTO>> getTherapistsByCategoryName(@RequestParam String category) {
//        return therapistProfileService.getTherapistsByCategoryName(category);
//    }

    @GetMapping("/search")
    public CommonResponse<List<TherapistProfile>> searchTherapists(@RequestParam(required = false) String name, @RequestParam(required = false) String location, @RequestParam(required = false) String category, @RequestParam(required = false) String price) throws UnexpectedServerException {
        return therapistProfileService.search(name, location, category, price);
    }

    // It returns only approved therapists, other apis may not, clarify
    @GetMapping("/getAllTherapists")
    public CommonResponse<List<AllTherapistsResponseDTO>> getAllTherapists() throws UnexpectedServerException {
        return therapistProfileService.getAllTherapists();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTherapist(@PathVariable Long id) throws ResourceNotFoundException {
        return therapistProfileService.deleteTherapist(id);
    }


}
