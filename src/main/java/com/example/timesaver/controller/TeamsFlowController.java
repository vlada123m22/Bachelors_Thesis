package com.example.timesaver.controller;

import com.example.timesaver.model.dto.teamflow.CreateTeamRequest;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamListingDTO;
import com.example.timesaver.model.Team;
import com.example.timesaver.service.TeamApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/teams-flow")
public class TeamsFlowController {
    private final TeamApplicationService service;

    public TeamsFlowController(TeamApplicationService service) {
        this.service = service;
    }

    // TODO: replace with actual current applicant resolution
    private Long currentApplicantId() { throw new IllegalStateException("Implement current applicant resolution"); }

    @PostMapping("/projects/{projectId}/teams")
    public ResponseEntity<Team> createTeam(@PathVariable Long projectId,
                                           @Valid @RequestBody CreateTeamRequest req) {
        Team team = service.createTeam(projectId, currentApplicantId(), req);
        return ResponseEntity.ok(team);
    }

    @PostMapping("/teams/{teamId}/applications")
    public ResponseEntity<Void> apply(@PathVariable Long teamId) {
        service.applyToTeam(teamId, currentApplicantId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/teams/{teamId}/applications/{appId}/decision")
    public ResponseEntity<Void> decide(@PathVariable Long teamId,
                                       @PathVariable Long appId,
                                       @RequestBody DecisionRequest req) {
        service.decideApplication(teamId, appId, currentApplicantId(), req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/teams/{teamId}/members/{memberId}")
    public ResponseEntity<Void> kick(@PathVariable Long teamId, @PathVariable Long memberId) {
        service.removeMember(teamId, memberId, currentApplicantId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/teams/{teamId}/members/{memberId}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long teamId, @PathVariable Long memberId) {
        service.leaveTeam(teamId, memberId, currentApplicantId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/projects/{projectId}/teams")
    public ResponseEntity<List<TeamListingDTO>> list(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.listTeams(projectId));
    }

}
