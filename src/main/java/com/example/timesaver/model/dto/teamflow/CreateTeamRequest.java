package com.example.timesaver.model.dto.teamflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateTeamRequest(
        @NotNull Integer projectId,
        @NotBlank String ideaTitle,
        String ideaDescription,
        List<RoleReq> roles,
        List<BackgroundReq> backgrounds
) {
    public record RoleReq(String code, Integer min, Integer max) {}
    public record BackgroundReq(String code, Integer min, Integer max) {}
}