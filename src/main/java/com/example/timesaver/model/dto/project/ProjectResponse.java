package com.example.timesaver.model.dto.project;

import lombok.Data;
import lombok.Getter;

@Getter
public class ProjectResponse {
    private final String status;
    private final String message;
    private Long projectId;

    public ProjectResponse(String status, String message, Long projectId) {
        this.status = status;
        this.message = message;
        this.projectId = projectId;
    }

    public ProjectResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}