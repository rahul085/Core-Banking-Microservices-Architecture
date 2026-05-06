package com.example.auth_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequest {
    @NotNull(message = "username is required")
    private String userName;
    @NotNull(message = "password is required")
    @Size(min = 8,message = "password should be at least 8 characters long ")
    private String password;
}
