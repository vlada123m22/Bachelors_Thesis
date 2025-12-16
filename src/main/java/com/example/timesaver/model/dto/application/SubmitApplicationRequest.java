package com.example.timesaver.model.dto.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SubmitApplicationRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Size(min = 1, max = 100, message = "Team name must be at least 1 and at most 100 characters")
    private String teamName;

    @NotNull(message = "Join existent team flag is required")
    private Boolean joinExistentTeam;

    private List<@Valid TeammateDTO> teammates;

    @NotNull(message = "Question answers are required")
    @Size(min = 1, message = "At least one answer is required")
    private List<@Valid QuestionAnswerDTO> questionsAnswers;

    // Timezone of the applicant (ISO format, e.g., "America/New_York")
    @NotBlank(message = "Timezone is required")
    private String timezone;
}