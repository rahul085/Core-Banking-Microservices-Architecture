package com.example.auth_service.service;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidTokenException;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.CustomUserDetailService;
import com.example.auth_service.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailService;

    public UserResponse registerUser(UserRegisterRequest userRegisterRequest){
        User user=new User();
        user.setUserName(userRegisterRequest.getUserName());
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));
        user.setEmail(userRegisterRequest.getEmail());
        user.setRoles(userRegisterRequest.getRoles().stream().map(role-> roleRepository.findByRoleName(role).orElseThrow(
                ()-> new ResourceNotFoundException("Role not found")
        ) ).collect(Collectors.toSet()));
        User savedUser = userRepository.save(user);

        UserResponse userResponse=new UserResponse();
        userResponse.setUserName(savedUser.getUserName());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setRoles(savedUser.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()));
        return userResponse;


    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Convert the Set<Role> into a Set<String> for your DTO
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        // Return your existing UserResponse!
        return new UserResponse(
                user.getUserName(),
                user.getEmail(),
                roleNames
        );
    }

    public LoginResponse login(LoginRequest loginRequest){
        LoginResponse response=new LoginResponse();
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
        UserDetails userDetails = (UserDetails)authenticate.getPrincipal();
        User user=userRepository.findByUserName(userDetails.getUsername()).orElseThrow(
                ()-> new ResourceNotFoundException("User with name "+userDetails.getUsername()+" not found")
        );

        response.setActiveToken(jwtUtil.generateActiveToken(userDetails,user.getEmail(),user.getUserId()));
        response.setRefreshToken(jwtUtil.generateRefreshToken(userDetails,user.getEmail(),user.getUserId()));
        return response;
    }

    public LoginResponse refreshToken(RefreshTokenRequest request){
        String refreshToken=request.getRefreshToken();
        LoginResponse response=new LoginResponse();
        if(jwtUtil.isTokenExpired(refreshToken)){
            throw new InvalidTokenException("Refresh token is expired. Please login again");
        }
        if(!"RefreshToken".equals(jwtUtil.getTokenType(refreshToken))){
            throw new InvalidTokenException("Invalid token type");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailService.loadUserByUsername(username);
        User user = userRepository.findByUserName(username).orElseThrow(() -> new ResourceNotFoundException("User with name " + username + " not found"));

        response.setActiveToken(jwtUtil.generateActiveToken(userDetails,user.getEmail(),user.getUserId()));
        response.setRefreshToken(jwtUtil.generateRefreshToken(userDetails,user.getEmail(),user.getUserId()));
        return response;

    }
}
