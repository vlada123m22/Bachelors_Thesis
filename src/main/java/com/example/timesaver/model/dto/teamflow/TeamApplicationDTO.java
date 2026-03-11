package com.example.timesaver.model.dto.teamflow;

import com.example.timesaver.model.TeamApplication.Status;
import java.time.ZonedDateTime;

public record TeamApplicationDTO(
        Long id,
        Long teamId,
        Long applicantId,
        String firstName,
        String lastName,
        Status status,
        ZonedDateTime appliedAt,
        ZonedDateTime decisionAt
) {}
