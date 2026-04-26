package org.zimtkoriander.bestellsystem.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private String username;
    private Set<String> roles;
    private long expiresIn;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username) {
        this.token = token;
        this.refreshToken = null;
        this.username = username;
        this.roles = Collections.emptySet();
        this.expiresIn = 0;
    }

    public AuthResponse(String token, String refreshToken, String username, Set<String> roles, long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.roles = roles == null ? Collections.emptySet() : new HashSet<>(roles);
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

