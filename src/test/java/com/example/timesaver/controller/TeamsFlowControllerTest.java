package com.example.timesaver.controller;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.Team;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.teamflow.CreateTeamRequest;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamApplicationDTO;
import com.example.timesaver.model.dto.teamflow.TeamListingDTO;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.TeamRepository;
import com.example.timesaver.repository.UserRepository;
import com.example.timesaver.service.TeamApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamsFlowControllerTest {

    @Mock private TeamApplicationService service;
    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private TeamsFlowController controller;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAuth(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        User user = new User();
        user.setId(1);
        user.setUserName(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
    }

    private void mockProjectAndApplicant(Integer projectId, Integer applicantId) {
        Project project = new Project();
        project.setProjectId(projectId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        when(applicantRepository.findIdByUserAndProject(any(), eq(project))).thenReturn(Optional.of(applicantId));
    }

    @Test
    public void testCreateTeamSuccess() {
        mockAuth("user");
        mockProjectAndApplicant(1, 10);
        CreateTeamRequest req = new CreateTeamRequest(1, "title", "desc", Collections.emptyList(), Collections.emptyList());

        ResponseEntity<String> response = controller.createTeam(1, req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testCreateTeamFailure() {
        mockAuth("user");
        mockProjectAndApplicant(1, 10);
        when(service.createTeam(anyInt(), anyInt(), any())).thenAnswer(invocation -> {
            throw new RuntimeException("Error");
        });

        CreateTeamRequest req = new CreateTeamRequest(1, "title", "desc", Collections.emptyList(), Collections.emptyList());
        ResponseEntity<String> response = controller.createTeam(1, req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error", response.getBody());
    }

    @Test
    public void testDecideFailure() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1, 10);
        doAnswer(invocation -> {
            throw new RuntimeException("Decision Error");
        }).when(service).decideApplication(anyInt(), anyInt(), anyInt(), any());

        ResponseEntity<String> response = controller.decide(100, 200, new DecisionRequest("ACCEPT", null, null));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Decision Error", response.getBody());
    }

    @Test
    public void testApply() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1, 10);

        ResponseEntity<Void> response = controller.apply(100);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(service).applyToTeam(100, 10);
    }

    @Test
    public void testDecide() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1, 10);

        ResponseEntity<String> response = controller.decide(100, 200, new DecisionRequest("ACCEPT", null, null));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testGetTeamApplications() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1, 10);
        when(service.getTeamApplications(anyInt(), anyInt())).thenReturn(Collections.emptyList());

        ResponseEntity<List<TeamApplicationDTO>> response = controller.getTeamApplications(100);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testKick() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1, 10);

        ResponseEntity<Void> response = controller.kick(100, 500);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testLeave() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1, 10);

        ResponseEntity<Void> response = controller.leave(100, 500);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testList() {
        when(service.listTeams(1)).thenReturn(Collections.emptyList());
        ResponseEntity<List<TeamListingDTO>> response = controller.list(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
