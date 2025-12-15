package com.example.timesaver.model.dto.auth;

public class LoginResponse {
    private String State;
    private String ErrorMessage;
    private String Token;  // JWT token added here

    public LoginResponse(String state, String errorMessage, String token) {
        State = state;
        ErrorMessage = errorMessage;
        Token = token;
    }

    public String getState() { return State; }
    public String getErrorMessage() { return ErrorMessage; }
    public String getToken() { return Token; }
}