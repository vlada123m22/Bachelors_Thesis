package com.example.timesaver.controller;

import com.example.timesaver.model.dto.auth.LoginRequest;
import com.example.timesaver.model.dto.auth.LoginResponse;
import com.example.timesaver.model.dto.auth.SignUpRequest;
import com.example.timesaver.model.dto.auth.SignUpResponse;
import com.example.timesaver.service.AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authService;

    @PostMapping("/signup/organizer")
    public ResponseEntity<SignUpResponse> signUpOrganizer(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.registerOrganizer(request);
        if (Objects.equals(response.getErrorMessage(), "The username already exists. Please choose another username") || Objects.equals(response.getErrorMessage(), "The email already exists. Please choose another email"))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        try {
            return  ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.registerOrganizer(request);
        if (Objects.equals(response.getErrorMessage(), "The username already exists. Please choose another username") || Objects.equals(response.getErrorMessage(), "The email already exists. Please choose another email"))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signup/participant")
    public ResponseEntity<SignUpResponse> signUpParticipant(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.registerParticipant(request);
        if ("The username already exists. Please choose another username".equals(response.getErrorMessage())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signup/admin")
    public SignUpResponse signUpAdmin(@RequestBody SignUpRequest request) {
        return authService.registerAdmin(request);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

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

    @PostMapping("/signup/mentor")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SignUpResponse> signUpMentor(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.registerMentor(request);
        if (!response.isCreated()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/change-password/{userName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@RequestBody com.example.timesaver.model.dto.auth.ChangePasswordRequest request,
                                                 @PathVariable String userName) {
        return authService.changePassword(userName, request.getOldPassword(), request.getNewPassword());
    }

    @DeleteMapping("/delete-profile/{userName}")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ORGANIZER')")
    public ResponseEntity<String> deleteParticipantProfile(@PathVariable String userName) {
        try {
            // Retrieve the currently authenticated user's authorities
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isParticipant = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PARTICIPANT"));
            boolean isOrganizer = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ORGANIZER"));

            if (isParticipant) {
                authService.deleteParticipantProfile(userName);
            } else if (isOrganizer) {
                authService.deleteOrganizerProfile(userName);
            } else {
                // This case should theoretically not happen because @PreAuthorize already restricts roles,
                // but it's included for safety.
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("User does not have a valid role for deletion.");
            }

            return ResponseEntity.ok("Profile deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting profile: " + e.getMessage());
        }
    }
}