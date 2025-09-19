package com.clinicalpsychology.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {

    @NotBlank(message = "Email cannot be empty or null")
    @Email(message = "Invalid email format")
    public String emailId;

    @NotBlank(message = "Password cannot be empty or null")
    public String password;
}
