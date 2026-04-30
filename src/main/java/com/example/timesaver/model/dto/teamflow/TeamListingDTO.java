package com.example.timesaver.model.dto.teamflow;

import java.util.List;

public record TeamListingDTO(
        Integer teamId,
        String teamName,
        String ideaTitle,
        Integer leadApplicantId,
        Integer size,
        Integer maxSize,
        Integer spotsLeft,
        List<ReqWithCounts> roles,
        List<ReqWithCounts> backgrounds
) {
    public record ReqWithCounts(String code, int min, int max, int assigned, int remainingMin, int remainingMax) {}
}