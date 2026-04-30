package com.example.timesaver.model.dto.project;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class ProjectResponse {
    private final String status;
    private final String message;
    private Integer projectId;

    private List<String> roleOptions;
    private List<String> backgroundOptions;
    private String rolesQuestionText;
    private String backgroundQuestionText;

    public ProjectResponse(String status, String message, Integer projectId) {
        this.status = status;
        this.message = message;
        this.projectId = projectId;
    }

    public ProjectResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}