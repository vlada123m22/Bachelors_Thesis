package com.example.timesaver.model.dto.project;


import com.example.timesaver.model.ScheduleVisibility;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

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

    private Boolean teamsPreformed;
    private ScheduleVisibility scheduleVisibility;

    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxNrParticipants;

    @Min(value = 1, message = "Min participants must be at least 1")
    private Integer minNrParticipants;

    private List<FormQuestionDTO> formQuestions;
    private List<ScheduleDTO> schedules;

    private List<String> roleOptions;       // e.g., ["Developer", "Designer"]
    private List<String> backgroundOptions; // e.g., ["Computer Science", "Marketing"]

    // Optional custom wording (organizer-defined). If null/blank, use defaults.
    private String rolesQuestionText;       // default: "What are your preferred roles in the team?"
    private String backgroundQuestionText;  // default: "What is your background?"

}

