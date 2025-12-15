package com.example.timesaver.controller;

import com.example.timesaver.model.dto.auth.LoginRequest;
import com.example.timesaver.model.dto.auth.LoginResponse;
import com.example.timesaver.model.dto.auth.SignUpRequest;
import com.example.timesaver.model.dto.auth.SignUpResponse;
import com.example.timesaver.service.AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authService;

    @PostMapping("/signup/organizer")
    public SignUpResponse signUpOrganizer(@RequestBody SignUpRequest request) {
        return authService.registerOrganizer(request);
    }

    @PostMapping("/signup")
    public SignUpResponse signUp(@RequestBody SignUpRequest request) {
        return authService.registerOrganizer(request);
    }

    @PostMapping("/signup/participant")
    public SignUpResponse signUpParticipant(@RequestBody SignUpRequest request) {
        return authService.registerParticipant(request);
    }

    @PostMapping("/signup/admin")
    public SignUpResponse signUpAdmin(@RequestBody SignUpRequest request) {
        return authService.registerAdmin(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Test endpoints for different roles
    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Authorization is working properly - Authenticated user");
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access granted");
    }

    @GetMapping("/organizer/test")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<String> organizerTest() {
        return ResponseEntity.ok("Organizer access granted");
    }

    @GetMapping("/participant/test")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ADMIN')")
    public ResponseEntity<String> participantTest() {
        return ResponseEntity.ok("Participant access granted");
    }
}