package org.zimtkoriander.bestellsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.dto.AuthResponse;
import org.zimtkoriander.bestellsystem.dto.LoginRequest;
import org.zimtkoriander.bestellsystem.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider tokenProvider;

    // Vereinfachter Login: In Produktion würde man echte Benutzer-DB verwenden
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // DEMO: Akzeptiere jeden User (in Produktion: Datenbank-Check + Password-Hashing)
        if (request.getUsername() != null && !request.getUsername().isEmpty() &&
            request.getPassword() != null && !request.getPassword().isEmpty()) {

            String token = tokenProvider.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, request.getUsername()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody LoginRequest request) {
        // DEMO: Registriere einfach jeden (in Produktion: validieren, in DB speichern, ...)
        if (request.getUsername() != null && !request.getUsername().isEmpty() &&
            request.getPassword() != null && !request.getPassword().isEmpty()) {

            String token = tokenProvider.generateToken(request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, request.getUsername()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}

