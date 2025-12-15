package com.example.timesaver.model.dto.auth;

public class SignUpResponse {
    private boolean Created;
    private String ErrorMessage;

    public SignUpResponse(boolean created, String errorMessage) {
        Created = created;
        ErrorMessage = errorMessage;
    }

    public boolean isCreated() { return Created; }
    public String getErrorMessage() { return ErrorMessage; }
}
