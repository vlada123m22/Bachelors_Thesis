package com.example.timesaver.service;

import com.example.timesaver.model.Role;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.auth.LoginRequest;
import com.example.timesaver.model.dto.auth.LoginResponse;
import com.example.timesaver.model.dto.auth.SignUpRequest;
import com.example.timesaver.model.dto.auth.SignUpResponse;
import com.example.timesaver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void testRegisterOrganizerSuccess() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("user");
        req.setPassword("pass");
        req.setEmail("email@test.com");
        
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        when(userRepository.findUserByEmail("email@test.com")).thenReturn(Optional.empty());
        when(encoder.encode("pass")).thenReturn("encoded");

        SignUpResponse resp = authenticationService.registerOrganizer(req);

        assertTrue(resp.isCreated());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterOrganizerDuplicateUsername() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("user");
        
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(new User()));

        SignUpResponse resp = authenticationService.registerOrganizer(req);

        assertFalse(resp.isCreated());
        assertEquals("The username already exists. Please choose another username", resp.getErrorMessage());
    }

    @Test
    public void testRegisterParticipantSuccess() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("user");
        req.setPassword("pass");
        req.setEmail("email@test.com");
        
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        when(userRepository.findUserByEmail("email@test.com")).thenReturn(Optional.empty());

        SignUpResponse resp = authenticationService.registerParticipant(req);
        assertTrue(resp.isCreated());
    }

    @Test
    public void testRegisterAdminSuccess() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("user");
        req.setPassword("pass");
        
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        SignUpResponse resp = authenticationService.registerAdmin(req);
        assertTrue(resp.isCreated());
    }

    @Test
    public void testRegisterMentorSuccess() {
        SignUpRequest req = new SignUpRequest();
        req.setUserName("user");
        req.setPassword("pass");
        req.setEmail("email@test.com");
        
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        when(userRepository.findUserByEmail("email@test.com")).thenReturn(Optional.empty());

        SignUpResponse resp = authenticationService.registerMentor(req);
        assertTrue(resp.isCreated());
    }

    @Test
    public void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUserName("user");
        req.setPassword("pass");
        
        User user = new User();
        user.setUserName("user");
        user.setPassword("encoded");
        user.setRoles(Set.of(Role.PARTICIPANT));

        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(encoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtService.generateToken(anyString(), any())).thenReturn("token");

        ResponseEntity<LoginResponse> resp = authenticationService.login(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Success", resp.getBody().getState());
        assertEquals("token", resp.getBody().getToken());
    }

    @Test
    public void testLoginUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setUserName("user");
        
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        ResponseEntity<LoginResponse> resp = authenticationService.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Failure", resp.getBody().getState());
    }

    @Test
    public void testLoginWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUserName("user");
        req.setPassword("pass");
        
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(encoder.matches("pass", "encoded")).thenReturn(false);

        ResponseEntity<LoginResponse> resp = authenticationService.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Incorrect password", resp.getBody().getErrorMessage());
    }
}
