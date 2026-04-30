package com.example.timesaver.model.dto.participant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class TeamApplicationStatusDTO {
    private Integer teamId;
    private String teamName;
    private String status;
    private ZonedDateTime appliedAt;
    private ZonedDateTime decisionAt;
}
