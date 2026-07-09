package com.taskflow.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OtpService {

    private static final String OTP_KEY_PREFIX = "otp:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final int otpExpiryMinutes;
    private final int otpLength;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${app.otp.expiry-minutes}") int otpExpiryMinutes,
            @Value("${app.otp.length}") int otpLength) {
        this.redisTemplate = redisTemplate;
        this.otpExpiryMinutes = otpExpiryMinutes;
        this.otpLength = otpLength;
    }

    public void generateAndSendOtp(String email) {
        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, otpExpiryMinutes, TimeUnit.MINUTES);
        log.info("OTP for {} : {} (valid for {} minutes)", email, otp, otpExpiryMinutes);
    }

    public boolean verifyOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email;
        Object storedOtp = redisTemplate.opsForValue().get(key);
        return storedOtp != null && storedOtp.toString().equals(otp);
    }

    public void invalidateOtp(String email) {
        redisTemplate.delete(OTP_KEY_PREFIX + email);
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
