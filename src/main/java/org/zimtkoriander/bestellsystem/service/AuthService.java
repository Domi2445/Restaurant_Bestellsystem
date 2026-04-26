package org.zimtkoriander.bestellsystem.service;

import Model.AppUser;
import Model.RefreshToken;
import Model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zimtkoriander.bestellsystem.dto.AuthResponse;
import org.zimtkoriander.bestellsystem.dto.LoginRequest;
import org.zimtkoriander.bestellsystem.dto.RefreshTokenRequest;
import org.zimtkoriander.bestellsystem.dto.RegisterRequest;
import org.zimtkoriander.bestellsystem.repository.AppUserRepository;
import org.zimtkoriander.bestellsystem.repository.RefreshTokenRepository;
import org.zimtkoriander.bestellsystem.security.JwtTokenProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.access-expiration:900000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration:1209600000}")
    private long refreshExpirationMs;

    public AuthService(AppUserRepository appUserRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String deviceInfo) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Username und Passwort sind erforderlich.");
        }

        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Benutzername ist bereits vergeben.");
        }

        String email = resolveEmail(request);
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-Mail ist bereits vergeben.");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        // Public registration always creates a customer account.
        user.setRoles(Set.of(Role.CUSTOMER));

        AppUser saved = appUserRepository.save(user);
        return createAuthResponse(saved, deviceInfo);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Ungultige Login-Daten.");
        }

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Benutzer oder Passwort ist falsch."));

        if (!user.isActive() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Benutzer oder Passwort ist falsch.");
        }

        return createAuthResponse(user, deviceInfo);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String deviceInfo) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh-Token fehlt.");
        }

        String hash = hashToken(request.getRefreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh-Token ist ungultig."));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            throw new IllegalArgumentException("Refresh-Token ist abgelaufen.");
        }

        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);

        return createAuthResponse(token.getUser(), deviceInfo);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return;
        }

        String hash = hashToken(refreshTokenValue);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    public AppUser getUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden."));
    }

    private AuthResponse createAuthResponse(AppUser user, String deviceInfo) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevoked(false);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                user.getUsername(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                accessExpirationMs
        );
    }

    private String hashToken(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Token konnte nicht gehasht werden.", e);
        }
    }

    private String resolveEmail(RegisterRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return request.getEmail().trim().toLowerCase();
        }
        return request.getUsername().trim().toLowerCase() + "@bestellsystem.local";
    }
}

