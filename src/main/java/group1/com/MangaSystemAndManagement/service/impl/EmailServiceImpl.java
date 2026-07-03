package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code for Registration");
        message.setText("Your OTP code is: " + otpCode + "\nIt will expire in 3 minutes.");
        
        mailSender.send(message);
    }
}
