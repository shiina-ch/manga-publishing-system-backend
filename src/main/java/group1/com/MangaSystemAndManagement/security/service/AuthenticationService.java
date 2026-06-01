package group1.com.MangaSystemAndManagement.security.service;

import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public String generateToken(Account account) {
        return jwtTokenProvider.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .roles("MANGAKA")
                .build()
        );
    }
}
