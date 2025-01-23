package com.movie.movieAPI.controllers;

import com.movie.movieAPI.auth.entities.RefreshToken;
import com.movie.movieAPI.auth.entities.User;
import com.movie.movieAPI.auth.repositories.RefreshTokenRepository;
import com.movie.movieAPI.auth.services.AuthService;
import com.movie.movieAPI.auth.services.JwtService;
import com.movie.movieAPI.auth.services.RefreshTokenService;
import com.movie.movieAPI.auth.utils.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest){

        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> register(@RequestBody LoginRequest loginRequest){

        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        try {
            refreshTokenService.logout(refreshToken);
            return ResponseEntity.ok().body("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while logging out");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // Retrieve the username and role from the authentication principal
        User user = (User) authentication.getPrincipal();
        // Retrieve the actual username and role
        String actualUsername = user.getActualUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        String role = authorities.isEmpty() ? "USER" : authorities.iterator().next().getAuthority();

        // Create a response object with minimal user info
        Map<String, String> response = new HashMap<>();
        response.put("username", actualUsername);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build());
    }



}
