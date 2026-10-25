package com.pet_project.watchstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendRegistrationCodeRequest(@NotBlank @Email String email) {
}
