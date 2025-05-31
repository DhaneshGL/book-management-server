package com.dhanesh.book.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegistrationRequest registrationRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
        );

        authenticationRequest = new AuthenticationRequest(
                "john.doe@example.com",
                "password123"
        );

        authenticationResponse = new AuthenticationResponse("jwt-token");
    }

    @Test
    void shouldRegisterUser() throws Exception {
        doNothing().when(authenticationService).register(any(RegistrationRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldAuthenticateUser() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldActivateAccount() throws Exception {
        String token = "123456";
        doNothing().when(authenticationService).activateAccount(anyString());

        mockMvc.perform(get("/auth/activate-account")
                        .param("token", token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestForInvalidRegistration() throws Exception {
        RegistrationRequest invalidRequest = new RegistrationRequest(
                "", "", "invalid-email", ""
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
