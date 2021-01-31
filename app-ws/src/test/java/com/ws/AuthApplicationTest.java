package com.ws;

import com.vs.model.UserInfo;
import com.vs.model.VerificationToken;
import com.vs.user.dto.AuthenticationResponse;
import com.vs.user.dto.LoginRequest;
import com.vs.user.dto.RefreshTokenRequest;
import com.vs.user.dto.RegisterRequest;
import com.vs.user.dto.RegisterResponse;
import com.vs.ws.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthApplicationTest extends AbstractVstoreTest {

    @Test
    public void test_userAuthentication() throws Exception {
        RegisterRequest registerRequest = createRegisterRequest();
        registerNewUser(registerRequest);
        verifyAccount(registerRequest.getUsername());
        AuthenticationResponse authenticationResponse = login(registerRequest);
        refreshToken(authenticationResponse);
    }

    private AuthenticationResponse login(RegisterRequest registerRequest) throws Exception {
        String url = getUrl("login");
        LoginRequest loginRequest = createLoginRequest(registerRequest);
        AuthenticationResponse authenticationResponse = sendPost(url, loginRequest, AuthenticationResponse.class);
        assertNotNull(authenticationResponse);
        return authenticationResponse;
    }

    private void refreshToken(AuthenticationResponse response) throws Exception {
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder()
                .username(response.getUsername())
                .refreshToken(response.getRefreshToken())
                .build();
        AuthenticationResponse authenticationResponse = sendPost(getUrl("refreshToken"), refreshTokenRequest, AuthenticationResponse.class);
        assertNotNull(authenticationResponse);
    }

    private LoginRequest createLoginRequest(RegisterRequest registerRequest) {
        return LoginRequest.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .build();
    }

    private void verifyAccount(String username) throws Exception {
        UserInfo userInfo = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("No user found with username: " + username));
        VerificationToken verificationToken = verificationTokenRepository.findByUser(userInfo).orElseThrow(
                () -> new ResourceNotFoundException("No token found for user: " + username));
        String token = verificationToken.getToken();
        String response = sendGet(AUTH_API_BASE + "/accountVerification/" + token, String.class);
        assertEquals("Account Activated Successfully", response);
    }

    public RegisterResponse registerNewUser(RegisterRequest registerRequest) throws Exception {
        RegisterResponse registerResponse = sendPost(AUTH_API_BASE + "/createUser", registerRequest, RegisterResponse.class);
        assertNotNull(registerResponse);
        return registerResponse;
    }

    protected RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .username("vsingh")
                .email("vsingh@gmail.com")
                .password("12345")
                .build();
    }

    public String getUrl(String endpoint) {
        return String.join("/", AUTH_API_BASE, endpoint);
    }
}
