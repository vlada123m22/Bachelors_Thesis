package com.example.timesaver.model.dto.project;

import com.example.timesaver.model.ScheduleVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class EditProjectRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 200, message = "Project name must be between 3 and 200 characters")
    private String projectName;

    private String projectDescription;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxNrParticipants;

    @Min(value = 1, message = "Min participants must be at least 1")
    private Integer minNrParticipants;

    @Valid
    @NotNull(message = "Form questions cannot be null")
    @Size(min = 1, message = "At least one form question is required")
    private List<FormQuestionDTO> formQuestions;

    private List<ScheduleDTO> schedules;

    private Boolean teamsPreformed;
    private ScheduleVisibility scheduleVisibility;

    private List<String> roleOptions;       // e.g., ["Developer", "Designer"]
    private List<String> backgroundOptions; // e.g., ["Computer Science", "Marketing"]

    // Optional custom wording (organizer-defined). If null/blank, use defaults.
    private String rolesQuestionText;       // default: "What are your preferred roles in the team?"
    private String backgroundQuestionText;  // default: "What is your background?"


}