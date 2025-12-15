package com.example.timesaver.model.dto.application;

import lombok.Getter;

@Getter
public class ApplicationResponse {
    private final String status;
    private final String message;
    private Boolean teamExists;
    private String teamName;
    private Long applicantId;

    public ApplicationResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApplicationResponse(String status, String message, Boolean teamExists, String teamName) {
        this.status = status;
        this.message = message;
        this.teamExists = teamExists;
        this.teamName = teamName;
    }

    public ApplicationResponse(String status, String message, Long applicantId) {
        this.status = status;
        this.message = message;
        this.applicantId = applicantId;
    }



}
