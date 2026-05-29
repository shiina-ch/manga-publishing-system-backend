package com.kilig.sba_assignment.security.service;

import com.kilig.sba_assignment.model.Teacher;
import com.kilig.sba_assignment.security.jwt.JwtTokenProvider;
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

    public String generateToken(Teacher teacher) {
        return jwtTokenProvider.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(teacher.getEmail())
                .password(teacher.getPassword())
                .roles("TEACHER")
                .build()
        );
    }
}
