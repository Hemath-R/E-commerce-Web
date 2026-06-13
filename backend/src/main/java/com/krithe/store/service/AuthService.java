package com.krithe.store.service;

import com.krithe.store.dto.AuthRequest;
import com.krithe.store.dto.AuthResponse;
import com.krithe.store.dto.RegisterRequest;
import com.krithe.store.entity.User;
import com.krithe.store.enums.Role;
import com.krithe.store.exception.BadRequestException;
import com.krithe.store.repository.UserRepository;
import com.krithe.store.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        log.info("Register request received for email={}", req.getEmail());
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        log.info("User registered: id={}, email={}", user.getId(), user.getEmail());
        return buildResponse(user);
    }

    public AuthResponse login(AuthRequest req) {
        log.info("Login attempt for email={}", req.getEmail());
        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        log.info("User lookup complete for email={} -> id={}", req.getEmail(), user.getId());
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("Password verification failed for userId={}", user.getId());
            throw new BadRequestException("Invalid email or password");
        }
        log.info("Password verified for userId={}", user.getId());
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        log.info("Generated JWT for userId={}", user.getId());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
