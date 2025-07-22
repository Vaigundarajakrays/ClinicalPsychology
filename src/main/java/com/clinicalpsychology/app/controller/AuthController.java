package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.ChangePasswordRequest;
import com.clinicalpsychology.app.dto.LoginRequest;
import com.clinicalpsychology.app.dto.LoginResponse;
import com.clinicalpsychology.app.enumUtil.OtpPurpose;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.AuthService;
import com.clinicalpsychology.app.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsersService usersService;
    private final AuthService authService;

    public AuthController(UsersService usersService, AuthService authService){
        this.usersService=usersService;
        this.authService=authService;
    }

//    @PostMapping("/signUp")
//    public CommonResponse<Users> signUp(@RequestBody Users users) throws UnexpectedServerException {
//        return usersService.signUp(users);
//    }

    @PostMapping("/adminSignUp")
    public CommonResponse<Users> adminCreate(@RequestBody Users users) throws UnexpectedServerException {
        return usersService.adminCreate(users);
    }

    @PostMapping("/login")
    public CommonResponse<LoginResponse> authenticate(@Valid @RequestBody LoginRequest loginRequest) throws UnexpectedServerException, ResourceNotFoundException {
        return authService.authenticate(loginRequest);
    }

    @PostMapping("/sendOtp")
    public CommonResponse<String> sendOtp(@RequestParam String email, @RequestParam String purpose) throws UnexpectedServerException {
        OtpPurpose otpPurpose = OtpPurpose.from(purpose);
        return authService.sendOtp(email, otpPurpose);
    }

//    @PostMapping("/sendOtp")
//    public CommonResponse<String> sendOtp(@RequestParam String email) throws UnexpectedServerException {
//        return authService.sendOtp(email);
//    }

    @PostMapping("/verifyOtp")
    public CommonResponse<String> verifyOtp(@RequestParam String email, @RequestParam String otp) throws UnexpectedServerException {
        return authService.verifyOtp(email, otp);
    }

    @PutMapping("/changePassword")
    public CommonResponse<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) throws UnexpectedServerException, ResourceNotFoundException {
        return authService.changePassword(changePasswordRequest);
    }
}
