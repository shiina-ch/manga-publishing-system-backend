package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.model.OtpVerification;
import group1.com.MangaSystemAndManagement.repository.OtpVerificationRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.EmailService;
import group1.com.MangaSystemAndManagement.service.interfaces.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    @Override
    public void generateAndSendOtp(String email) {
        String otpCode = String.format("%06d", random.nextInt(1000000));
        
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setExpiredAt(LocalDateTime.now().plusMinutes(3));
        otpVerification.setUsed(false);
        
        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(email, otpCode);
    }

    @Override
    public void validateOtp(String email, String otpCode) {
        Optional<OtpVerification> otpVerificationOpt = otpVerificationRepository
                .findTopByEmailAndIsUsedFalseOrderByExpiredAtDesc(email);
                
        if (otpVerificationOpt.isEmpty()) {
            throw new IllegalArgumentException("OTP not found or already used");
        }
        
        OtpVerification otpVerification = otpVerificationOpt.get();
        
        if (LocalDateTime.now().isAfter(otpVerification.getExpiredAt())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        
        if (!otpVerification.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("Invalid OTP code");
        }
        
        otpVerification.setUsed(true);
        otpVerificationRepository.save(otpVerification);
    }
}
