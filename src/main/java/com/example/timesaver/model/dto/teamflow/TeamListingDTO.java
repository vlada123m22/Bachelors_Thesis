package com.example.timesaver.model.dto.teamflow;

import java.util.List;

public record TeamListingDTO(
        Long teamId,
        String teamName,
        String ideaTitle,
        Long leadApplicantId,
        int size,
        int maxSize,
        int spotsLeft,
        List<ReqWithCounts> roles,
        List<ReqWithCounts> backgrounds
) {
    public record ReqWithCounts(String code, int min, int max, int assigned, int remainingMin, int remainingMax) {}
}