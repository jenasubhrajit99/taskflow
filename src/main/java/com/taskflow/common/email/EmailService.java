package com.taskflow.common.email;

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
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendOtpEmail(String to, String otp, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "TaskFlow");
            helper.setTo(to);
            helper.setSubject("Your TaskFlow verification code");
            helper.setText(buildOtpHtml(otp, expiryMinutes), true);
            mailSender.send(message);
            log.debug("OTP email sent to {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOtpHtml(String otp, int expiryMinutes) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:40px 0">
                    <tr>
                      <td align="center">
                        <table width="480" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,.08)">
                          <tr>
                            <td align="center" style="padding-bottom:24px">
                              <h1 style="margin:0;font-size:24px;color:#1a1a2e">TaskFlow</h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="color:#333;font-size:15px;line-height:1.6">
                              <p style="margin:0 0 16px">Hi,</p>
                              <p style="margin:0 0 24px">
                                Use the code below to verify your email address.
                                The code expires in <strong>%d minutes</strong>.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td align="center" style="padding:24px 0">
                              <div style="display:inline-block;background:#6366f1;color:#fff;
                                          font-size:32px;font-weight:700;letter-spacing:12px;
                                          padding:16px 32px;border-radius:8px">
                                %s
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="color:#777;font-size:13px;padding-top:24px;border-top:1px solid #eee">
                              <p style="margin:16px 0 0">
                                If you did not request this, you can safely ignore this email.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(expiryMinutes, otp);
    }
}
