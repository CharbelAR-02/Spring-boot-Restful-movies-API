package com.movie.movieAPI.auth.services;

import com.movie.movieAPI.auth.entities.User;
import com.movie.movieAPI.auth.entities.UserRole;
import com.movie.movieAPI.auth.repositories.RefreshTokenRepository;
import com.movie.movieAPI.auth.repositories.UserRepository;
import com.movie.movieAPI.auth.utils.AuthResponse;
import com.movie.movieAPI.auth.utils.LoginRequest;
import com.movie.movieAPI.auth.utils.LoginResponse;
import com.movie.movieAPI.auth.utils.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public LoginResponse register(RegisterRequest registerRequest){

        var user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .build();

        User saveUser = userRepository.save(user);

        var accessToken = jwtService.generateToken(user);

        var refreshToken = refreshTokenService.createRefreshToken(saveUser.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .username(user.getActualUsername())
                .role(String.valueOf(user.getRole()))
                .build();
    }

    public LoginResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found !"));
        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(loginRequest.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .username(user.getActualUsername())
                .role(String.valueOf(user.getRole()))
                .build();
    }

}
