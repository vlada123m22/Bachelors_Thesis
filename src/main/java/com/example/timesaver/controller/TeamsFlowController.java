package com.example.timesaver.controller;

import com.example.timesaver.model.dto.teamflow.CreateTeamRequest;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamApplicationDTO;
import com.example.timesaver.model.dto.teamflow.TeamListingDTO;
import com.example.timesaver.model.Team;
import com.example.timesaver.model.User;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.Applicant;
import com.example.timesaver.repository.UserRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.TeamRepository;
import com.example.timesaver.service.TeamApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/teams-flow")
public class TeamsFlowController {
    private final TeamApplicationService teamApplicationService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicantRepository applicantRepository;
    private final TeamRepository teamRepository;

    public TeamsFlowController(TeamApplicationService teamApplicationService,
                               UserRepository userRepository,
                               ProjectRepository projectRepository,
                               ApplicantRepository applicantRepository,
                               TeamRepository teamRepository) {
        this.teamApplicationService = teamApplicationService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.applicantRepository = applicantRepository;
        this.teamRepository = teamRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String username = auth.getName();
        return userRepository.findByUserName(username).orElse(null);
    }

    private Integer currentApplicantId(Integer projectId) {
        User user = getCurrentUser();
        if (user == null) throw new SecurityException("User not authenticated");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));


        return applicantRepository.findIdByUserAndProject(user, project)
                .orElseThrow(() -> new IllegalStateException("User has not applied for this project"));
    }

    @PostMapping("/projects/{projectId}/teams")
    public ResponseEntity<String> createTeam(@PathVariable Integer projectId,
                                             @Valid @RequestBody CreateTeamRequest req) {

        try {
            Team team = teamApplicationService.createTeam(projectId, currentApplicantId(projectId), req);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }

    }

    @PostMapping("/teams/{teamId}/applications")
    public ResponseEntity<Void> apply(@PathVariable Integer teamId) {
        // We need project ID to resolve applicant ID. Let's find project from teamId.
        // Usually, the API should probably have project ID if it follows the pattern.
        // For now, let's find the team to get the project ID.
        // Alternatively, we could update the service to take User instead of applicantId.
        Integer projectId = findProjectIdByTeamId(teamId);
        teamApplicationService.applyToTeam(teamId, currentApplicantId(projectId));
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/teams/{teamId}/applications/{appId}/decision")
    public ResponseEntity<String> decide(@PathVariable Integer teamId,
                                         @PathVariable Integer appId,
                                         @RequestBody DecisionRequest req) {
        try {
            Integer projectId = findProjectIdByTeamId(teamId);
            teamApplicationService.decideApplication(teamId, appId, currentApplicantId(projectId), req);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/teams/{teamId}/applications")
    public ResponseEntity<List<TeamApplicationDTO>> getTeamApplications(@PathVariable Integer teamId) {
        Integer projectId = findProjectIdByTeamId(teamId);
        return ResponseEntity.ok(teamApplicationService.getTeamApplications(teamId, currentApplicantId(projectId)));
    }

    @DeleteMapping("/teams/{teamId}/members/{memberId}")
    public ResponseEntity<Void> kick(@PathVariable Integer teamId, @PathVariable Integer memberId) {
        Integer projectId = findProjectIdByTeamId(teamId);
        teamApplicationService.removeMember(teamId, memberId, currentApplicantId(projectId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/teams/{teamId}/members/{memberId}/leave")
    public ResponseEntity<Void> leave(@PathVariable Integer teamId, @PathVariable Integer memberId) {
        Integer projectId = findProjectIdByTeamId(teamId);
        teamApplicationService.leaveTeam(teamId, memberId, currentApplicantId(projectId));
        return ResponseEntity.ok().build();
    }

    private Integer findProjectIdByTeamId(Integer teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        return team.getProject().getProjectId();
    }

    @GetMapping("/projects/{projectId}/teams")
    public ResponseEntity<List<TeamListingDTO>> list(@PathVariable Integer projectId) {
        return ResponseEntity.ok(teamApplicationService.listTeams(projectId));
    }

}
