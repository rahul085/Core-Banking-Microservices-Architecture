package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    // These now perfectly match what the Auth Service sends!
    private String userName;
    private String email;
    private Set<String> roles;
}
