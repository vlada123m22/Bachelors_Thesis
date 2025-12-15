package com.example.timesaver.model.dto.project;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class GetProjectResponse {
    private final Long projectId;
    private final String projectName;
    private final Integer maxNrParticipants;
    private final Integer minNrParticipants;
    private final List<FormQuestionDTO> formQuestions;
    private final String projectDescription;
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;

    public GetProjectResponse(Long projectId, String projectName,  String projectDescription, ZonedDateTime startDate,
                              ZonedDateTime endDate,Integer maxNrParticipants,
                              Integer minNrParticipants, List<FormQuestionDTO> formQuestions) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxNrParticipants = maxNrParticipants;
        this.minNrParticipants = minNrParticipants;
        this.formQuestions = formQuestions;

    }
}