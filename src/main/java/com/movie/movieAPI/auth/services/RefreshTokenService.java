package com.movie.movieAPI.auth.services;

import com.movie.movieAPI.auth.entities.RefreshToken;
import com.movie.movieAPI.auth.entities.User;
import com.movie.movieAPI.auth.repositories.RefreshTokenRepository;
import com.movie.movieAPI.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username){
        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->new UsernameNotFoundException("User not found with email: " + username));

        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken == null){
            long RefreshTokenValidity = 14 * 24 * 60 * 60 * 1000; // 14 days in milliseconds
            refreshToken = RefreshToken.builder()
                    .refreshToken(UUID.randomUUID().toString())
                    .expirationTime(Instant.now().plusMillis(RefreshTokenValidity))
                    .user(user)
                    .build();

            refreshTokenRepository.save(refreshToken);

        }

        return refreshToken;
    }

    public RefreshToken verifyRefreshToken(String refreshToken){

        RefreshToken refToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found!"));

        if (refToken.getExpirationTime().compareTo(Instant.now()) < 0){
            //expired
            refreshTokenRepository.delete(refToken);
            throw new RuntimeException("Refresh Token Expired");
        }

        return refToken;
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }
}
