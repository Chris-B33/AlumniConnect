package com.alumniconnect.identityservice.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.identityservice.config.JwtProperties;
import com.alumniconnect.identityservice.domain.User;
import com.alumniconnect.identityservice.domain.UserRole;
import com.alumniconnect.identityservice.dto.LoginRequest;
import com.alumniconnect.identityservice.dto.RegisterRequest;
import com.alumniconnect.identityservice.dto.TokenResponse;
import com.alumniconnect.identityservice.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public TokenResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        UserRole role = UserRole.fromApi(request.role());
        User user = User.newUser(email, passwordEncoder.encode(request.password()), role);
        users.save(user);
        return tokenFor(user);
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return tokenFor(user);
    }

    private TokenResponse tokenFor(User user) {
        String token = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        return new TokenResponse(token, "Bearer", jwtProperties.expirationSeconds());
    }
}
