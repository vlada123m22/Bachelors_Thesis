package com.example.timesaver.service;

import com.example.timesaver.model.Role;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.auth.LoginRequest;
import com.example.timesaver.model.dto.auth.LoginResponse;
import com.example.timesaver.model.dto.auth.SignUpRequest;
import com.example.timesaver.model.dto.auth.SignUpResponse;
import com.example.timesaver.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder encoder;

    private final JwtService jwtService;

    private final UserService userService;

    public AuthenticationService(UserRepository userRepository, BCryptPasswordEncoder encoder, JwtService jwtService, UserService userService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public SignUpResponse registerOrganizer(SignUpRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            return new SignUpResponse(false,
                    "The username already exists. Please choose another username");
        }

//        if(userRepository.findUserByEmail(request.getEmail()).isPresent())
//            return new SignUpResponse(false,
//                    "The email already exists. Please choose another email");

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreationDateTime(LocalDateTime.now());
        user.setRoles(Set.of(Role.ORGANIZER));
        user.setEmail(request.getEmail());


        userRepository.save(user);
        return new SignUpResponse(true, null);
    }

    public SignUpResponse registerParticipant(SignUpRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            return new SignUpResponse(false,
                    "The username already exists. Please choose another username");
        }

//        if(userRepository.findUserByEmail(request.getEmail()).isPresent())
//            return new SignUpResponse(false,
//                    "The email already exists. Please choose another email");
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreationDateTime(LocalDateTime.now());
        user.setRoles(Set.of(Role.PARTICIPANT));

        userRepository.save(user);

        return new SignUpResponse(true, null);
    }

    public SignUpResponse registerAdmin(SignUpRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            return new SignUpResponse(false,
                    "The username already exists. Please choose another username");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreationDateTime(LocalDateTime.now());
        user.setRoles(Set.of(Role.ADMIN));

        userRepository.save(user);

        return new SignUpResponse(true, null);
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUserName(request.getUserName());
        LoginResponse body;
        if (userOpt.isEmpty()) {
            body =  new LoginResponse("Failure", "User not found", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        User user = userOpt.get();

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            body = new LoginResponse("Failure", "Incorrect password", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getUserName(), user.getRoles());

        body = new LoginResponse("Success", null, token);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    public SignUpResponse registerMentor(SignUpRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            return new SignUpResponse(false, "The username already exists. Please choose another username");
        }
//        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
//            return new SignUpResponse(false, "The email already exists. Please choose another email");
//        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreationDateTime(LocalDateTime.now());
        user.setRoles(Set.of(Role.MENTOR));
        user.setEmail(request.getEmail());

        userRepository.save(user);
        return new SignUpResponse(true, null);
    }

    public ResponseEntity<String> changePassword(String userName, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();
        if (!encoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect old password");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    public void deleteParticipantProfile(String userName) {
        userService.deleteParticipantProfile(userName);
    }

    public void deleteOrganizerProfile(String userName) {
        userService.deleteOrganizerProfile(userName);
    }
}