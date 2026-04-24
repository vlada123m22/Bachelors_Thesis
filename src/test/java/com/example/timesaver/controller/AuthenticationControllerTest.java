package com.example.timesaver.controller;

import com.example.timesaver.model.dto.auth.LoginRequest;
import com.example.timesaver.model.dto.auth.LoginResponse;
import com.example.timesaver.model.dto.auth.SignUpRequest;
import com.example.timesaver.model.dto.auth.SignUpResponse;
import com.example.timesaver.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    public void testSignUpOrganizerSuccess() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("user");
        SignUpResponse resp = new SignUpResponse(true, null);
        
        when(authService.registerOrganizer(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpOrganizer(req);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isCreated());
    }

    @Test
    public void testSignUpOrganizerConflict() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "The username already exists. Please choose another username");
        
        when(authService.registerOrganizer(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpOrganizer(req);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSignUp() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(true, null);
        when(authService.registerOrganizer(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUp(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testSignUpParticipant() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(true, null);
        when(authService.registerParticipant(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpParticipant(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testSignUpParticipantConflict() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "The username already exists. Please choose another username");
        when(authService.registerParticipant(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpParticipant(req);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSignUpAdmin() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(true, null);
        when(authService.registerAdmin(any())).thenReturn(resp);

        SignUpResponse response = authenticationController.signUpAdmin(req);
        assertTrue(response.isCreated());
    }

    @Test
    public void testLogin() {
        LoginRequest req = new LoginRequest();
        LoginResponse resp = new LoginResponse("Success", null, "token");
        when(authService.login(any())).thenReturn(ResponseEntity.ok(resp));

        ResponseEntity<LoginResponse> response = authenticationController.login(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", response.getBody().getToken());
    }

    @Test
    public void testTestEndpoints() {
        assertEquals(HttpStatus.OK, authenticationController.test().getStatusCode());
        assertEquals(HttpStatus.OK, authenticationController.adminTest().getStatusCode());
        assertEquals(HttpStatus.OK, authenticationController.organizerTest().getStatusCode());
        assertEquals(HttpStatus.OK, authenticationController.participantTest().getStatusCode());
    }

    @Test
    public void testSignUpMentor() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(true, null);
        when(authService.registerMentor(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpMentor(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testSignUpMentorConflict() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "Error");
        when(authService.registerMentor(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpMentor(req);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSignUpOrganizerEmailConflict() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "The email already exists. Please choose another email");

        when(authService.registerOrganizer(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpOrganizer(req);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSignUpEmailConflict() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "The email already exists. Please choose another email");
        when(authService.registerOrganizer(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUp(req);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSignUpConflict() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "The username already exists. Please choose another username");
        when(authService.registerOrganizer(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUp(req);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSignUpParticipantOtherError() {
        SignUpRequest req = new SignUpRequest();
        SignUpResponse resp = new SignUpResponse(false, "Some other error");
        when(authService.registerParticipant(any())).thenReturn(resp);

        ResponseEntity<SignUpResponse> response = authenticationController.signUpParticipant(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
