package com.example.timesaver.model.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class ProjectDashboardDTO {
    private Integer projectId;
    private String projectName;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String projectDescription;
    private Integer maxTeamSize;
    private Integer minTeamSize;
}
