package com.vs.ws.controller;

import com.vs.service.user.RefreshTokenService;
import com.vs.service.user.UserService;
import com.vs.user.dto.AuthenticationResponse;
import com.vs.user.dto.LoginRequest;
import com.vs.user.dto.RefreshTokenRequest;
import com.vs.user.dto.RegisterRequest;
import com.vs.user.dto.RegisterResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private UserService userService;
    private RefreshTokenService refreshTokenService;

    @GetMapping("/ping")
    public String ping() {
        return LocalDateTime.now().toString();
    }

    @PostMapping("/createUser")
    public ResponseEntity<RegisterResponse> createUser(@RequestBody RegisterRequest userRequest) {
        log.debug("In createUser(): {}", userRequest);
        RegisterResponse registerResponse = userService.createUser(userRequest);
        log.debug("Out createUser()");
        return ResponseEntity.ok(registerResponse);
    }

    @GetMapping("accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        log.debug("in verifyAccount...");
        userService.verifyAccount(token);
        return new ResponseEntity<>("Account Activated Successfully", OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok().body(userService.login(loginRequest));
    }

    @PostMapping("refreshToken")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse authenticationResponse = userService.refreshToken(request);
        return new ResponseEntity<>(authenticationResponse, OK);
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<RegisterResponse>> users() {
        List<RegisterResponse> list = userService.getUsers();
        return ResponseEntity.ok(list);
    }
}
