package com.example.auth_service.controller;


import com.example.auth_service.dto.*;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest){
        return new ResponseEntity<>(authService.registerUser(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return new ResponseEntity<>(authService.login(loginRequest),HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
        return new ResponseEntity<>(authService.refreshToken(request),HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = authService.getUserById(userId);
        return ResponseEntity.ok(response);
    }



}
