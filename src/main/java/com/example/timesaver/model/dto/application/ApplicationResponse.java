package com.example.timesaver.model.dto.application;

import lombok.Getter;

@Getter
public class ApplicationResponse {
    private final String status;
    private final String message;
    private Boolean teamExists;

    public ApplicationResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    public ApplicationResponse(String status, String message, Boolean teamExists) {
        this.status = status;
        this.message = message;
        this.teamExists = teamExists;
    }

}
