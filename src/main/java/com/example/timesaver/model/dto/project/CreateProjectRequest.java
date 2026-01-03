package com.example.timesaver.model.dto.project;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 200, message = "Project name must be between 1 and 200 characters")
    private String projectName;

    @Size(max = 2000, message = "Project description must be less than 2000 characters")
    private String projectDescription;

    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxNrParticipants;

    @Min(value = 1, message = "Min participants must be at least 1")
    private Integer minNrParticipants;

    private List<FormQuestionDTO> formQuestions;
}

