package com.example.timesaver.model.dto.teamflow;

import java.util.List;

public record DecisionRequest(
        String decision, // ACCEPT or REJECT
        List<String> assignRoles,
        List<String> assignBackgrounds
) {}