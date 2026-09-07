package com.bteam.platform.core.auth.port;

public interface MailSender {
    void sendResetPasswordEmail(String toEmail, String token);
}
