package com.example.timesaver.model.dto.auth;


import com.example.timesaver.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String State;
    private String ErrorMessage;
    private String Token;  // JWT token added here
    private Set<Role> roles;

    public LoginResponse(String state, String errorMessage, String token) {
        State = state;
        ErrorMessage = errorMessage;
        Token = token;
    }

    public String getState() { return State; }
    public String getErrorMessage() { return ErrorMessage; }
    public String getToken() { return Token; }
}