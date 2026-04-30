package com.example.timesaver.model.dto.participant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class ParticipantProjectStatusDTO {
    private Integer projectId;
    private String projectName;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String projectDescription;
    private Boolean hasApplied;
    private Boolean isAccepted;
}
