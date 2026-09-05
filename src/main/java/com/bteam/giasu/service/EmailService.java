package com.bteam.giasu.service;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        String resetLink="http://localhost:3000/reset-password?token="+token;
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - GiaSu Connect");
        message.setText("""
            Xin chào,
            
            Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản GiaSu Connect.
            
            Vui lòng click vào link sau để đặt lại mật khẩu:
            %s
            
            Link này sẽ hết hạn sau 30 phút.
            
            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
            
            Trân trọng,
            GiaSu Connect Team
            """.formatted(resetLink));

        mailSender.send(message);
    }
}
