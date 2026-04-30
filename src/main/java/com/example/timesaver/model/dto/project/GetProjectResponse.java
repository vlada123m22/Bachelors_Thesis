package com.example.timesaver.model.dto.project;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class GetProjectResponse {
    private  Integer projectId;
    private  String projectName;
    private  Integer maxNrParticipants;
    private  Integer minNrParticipants;
    private  List<FormQuestionDTO> formQuestions;
    private  String projectDescription;
    private  ZonedDateTime startDate;
    private  ZonedDateTime endDate;

    public GetProjectResponse(Integer projectId, String projectName,  String projectDescription, ZonedDateTime startDate,
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

    //To be used in ProjectRepository
    public GetProjectResponse(Integer projectId, String projectName,  String projectDescription, ZonedDateTime startDate,
                              ZonedDateTime endDate,Integer maxNrParticipants,
                              Integer minNrParticipants) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxNrParticipants = maxNrParticipants;
        this.minNrParticipants = minNrParticipants;
    }
}