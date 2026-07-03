package group1.com.MangaSystemAndManagement.service.interfaces;

public interface OtpService {
    void generateAndSendOtp(String email);
    void validateOtp(String email, String otpCode);
}
