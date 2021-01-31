package com.vs.service.user;

import com.vs.jwt.JwtProvider;
import com.vs.model.UserInfo;
import com.vs.model.VerificationToken;
import com.vs.service.config.ServiceProperties;
import com.vs.service.mapper.UserMapper;
import com.vs.service.repository.UserRepository;
import com.vs.service.repository.VerificationTokenRepository;
import com.vs.user.dto.AuthenticationResponse;
import com.vs.user.dto.LoginRequest;
import com.vs.user.dto.RefreshTokenRequest;
import com.vs.user.dto.RegisterRequest;
import com.vs.user.dto.RegisterResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
@EnableConfigurationProperties(ServiceProperties.class)
public class UserService {

    private final ServiceProperties serviceProperties;
    private UserMapper userMapper;
    private UserRepository userRepository;
    private VerificationTokenRepository verificationTokenRepository;
    private JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private RefreshTokenService refreshTokenService;

    public RegisterResponse createUser(RegisterRequest userRequest) {
        UserInfo userInfo = userRepository.save(userMapper.map(userRequest));
        String token = generateVerificationToken(userInfo);
        log.trace("verificationToken : {}", token);
        return userMapper.map(userInfo);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(loginRequest.getUsername())
                .build();
    }

    private String generateVerificationToken(UserInfo userInfo) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(userInfo);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public List<RegisterResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(userInfo -> userMapper.map(userInfo))
                .collect(Collectors.toList());
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() -> new RuntimeException("Invalid Token!")));
    }

    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        UserInfo user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found with name: " + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        refreshTokenService.validateRefreshToken(request.getRefreshToken());
        String token = jwtProvider.generateTokenWithUsername(request.getUsername());
        return AuthenticationResponse.builder()
                .username(request.getUsername())
                .authenticationToken(token)
                .refreshToken(request.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .build();
    }
}
