package com.farmmarket.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otp, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP for Login - FarmMarket");
            helper.setText(buildOtpEmailTemplate(otp, userName), true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailTemplate(String otp, String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Your OTP Code</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); padding: 40px; text-align: center; border-radius: 10px 10px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">
                                                🌾 FarmMarket
                                            </h1>
                                            <p style="margin: 10px 0 0 0; color: #e0f2e9; font-size: 14px;">
                                                Farm to Home Marketplace
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px 40px;">
                                            <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333;">
                                                Hello%s,
                                            </p>
                                            <p style="margin: 0 0 20px 0; font-size: 16px; color: #555555; line-height: 1.6;">
                                                You requested to log in to your FarmMarket account. Please use the following One-Time Password (OTP) to complete your login:
                                            </p>
                                            
                                            <!-- OTP Box -->
                                            <div style="background-color: #f8fffe; border: 2px dashed #10b981; border-radius: 8px; padding: 25px; text-align: center; margin: 30px 0;">
                                                <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666; text-transform: uppercase; letter-spacing: 1px;">
                                                    Your OTP Code
                                                </p>
                                                <p style="margin: 0; font-size: 36px; font-weight: 700; color: #10b981; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                                                    %s
                                                </p>
                                            </div>
                                            
                                            <p style="margin: 20px 0 0 0; font-size: 14px; color: #666666; line-height: 1.6;">
                                                ⏱️ <strong>This OTP will expire in 5 minutes</strong> for your security.
                                            </p>
                                            <p style="margin: 15px 0 0 0; font-size: 14px; color: #666666; line-height: 1.6;">
                                                If you didn't request this OTP, please ignore this email. Your account remains secure.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f9fafb; padding: 30px 40px; border-radius: 0 0 10px 10px; border-top: 1px solid #e5e7eb;">
                                            <p style="margin: 0 0 10px 0; font-size: 13px; color: #888888; text-align: center;">
                                                🛡️ <strong>Security Tip:</strong> Never share your OTP with anyone.
                                            </p>
                                            <p style="margin: 0; font-size: 12px; color: #999999; text-align: center;">
                                                © 2026 FarmMarket. All rights reserved.<br>
                                                Fresh from Farm to Your Home
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(userName != null ? " " + userName : "", otp);
    }
}
