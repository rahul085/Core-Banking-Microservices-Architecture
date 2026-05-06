package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRegisterRequest {

    @NotNull(message = "user name is required")
    private String userName;
    @NotNull(message = "password is required")
    private String password;
    @NotNull(message = "email is required")
    @Email
    private String email;
    @NotNull(message = "roles are required")
    private Set<String> roles;
}
