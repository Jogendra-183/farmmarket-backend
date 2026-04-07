package com.farmmarket.controller;

import com.farmmarket.dto.request.LoginRequest;
import com.farmmarket.dto.request.RegisterRequest;
import com.farmmarket.dto.request.SendOtpRequest;
import com.farmmarket.dto.request.VerifyOtpRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.AuthResponse;
import com.farmmarket.entity.User;
import com.farmmarket.security.JwtUtils;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.AuthService;
import com.farmmarket.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtUtils jwtUtils;

    /**
     * POST /api/auth/register
     * Register a new user (buyer or farmer)
     * Body: { name, email, password, role }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    /**
     * POST /api/auth/login
     * Login with email, password, and role
     * Body: { email, password, role }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/auth/send-otp
     * Send OTP to email for passwordless login
     * Body: { email }
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully to " + request.getEmail()));
    }

    /**
     * POST /api/auth/verify-otp
     * Verify OTP and authenticate user
     * Body: { email, otp }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        User user = otpService.verifyOtp(request.getEmail(), request.getOtp());
        
        String token = jwtUtils.generateTokenFromEmail(user.getEmail());
        
        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImageUrl())
                .farmName(user.getFarmName())
                .createdAt(user.getCreatedAt())
                .build();

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/auth/resend-otp
     * Resend OTP to email (with cooldown)
     * Body: { email }
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully to " + request.getEmail()));
    }

    /**
     * GET /api/auth/me
     * Get current authenticated user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AuthResponse.UserDto profile = authService.getProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
