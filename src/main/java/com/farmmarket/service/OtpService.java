package com.farmmarket.service;

import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.UnauthorizedException;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts}")
    private int maxOtpAttempts;

    @Value("${app.otp.resend-cooldown-seconds}")
    private int resendCooldownSeconds;

    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            throw new BadRequestException("No account found with this email address.");
        }

        if (user.getLastOtpSent() != null) {
            long secondsSinceLastOtp = Duration.between(user.getLastOtpSent(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLastOtp < resendCooldownSeconds) {
                long remainingSeconds = resendCooldownSeconds - secondsSinceLastOtp;
                throw new BadRequestException("Please wait " + remainingSeconds + " seconds before requesting a new OTP");
            }
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        user.setOtpAttempts(0);
        user.setLastOtpSent(LocalDateTime.now());

        userRepository.save(user);

        emailService.sendOtpEmail(email, otp, user.getName());
        log.info("OTP generated and sent to email: {}", email);
    }

    @Transactional
    public User verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No OTP request found for this email"));

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw new BadRequestException("No active OTP found. Please request a new OTP");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            user.setOtp(null);
            user.setOtpExpiry(null);
            user.setOtpAttempts(0);
            userRepository.save(user);
            throw new BadRequestException("OTP has expired. Please request a new OTP");
        }

        if (user.getOtpAttempts() >= maxOtpAttempts) {
            user.setOtp(null);
            user.setOtpExpiry(null);
            user.setOtpAttempts(0);
            userRepository.save(user);
            throw new UnauthorizedException("Maximum OTP verification attempts exceeded. Please request a new OTP");
        }

        if (!user.getOtp().equals(otp)) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userRepository.save(user);
            int remainingAttempts = maxOtpAttempts - user.getOtpAttempts();
            throw new BadRequestException("Invalid OTP. " + remainingAttempts + " attempt(s) remaining");
        }

        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setOtpAttempts(0);
        user.setIsActive(true);
        userRepository.save(user);

        log.info("OTP verified successfully for email: {}", email);
        return user;
    }

    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
