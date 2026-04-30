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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
        user.setId(1L);
        user.setUserName(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
    }

    private void mockProjectAndApplicant(Long projectId, Integer applicantId) {
        Project project = new Project();
        project.setProjectId(projectId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        when(applicantRepository.findByUserAndProject(any(), eq(project))).thenReturn(Optional.of(applicant));
    }

    @Test
    public void testCreateTeamSuccess() {
        mockAuth("user");
        mockProjectAndApplicant(1L, 10L);
        CreateTeamRequest req = new CreateTeamRequest(1L, "title", "desc", Collections.emptyList(), Collections.emptyList());
        
        ResponseEntity<String> response = controller.createTeam(1L, req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testCreateTeamFailure() {
        mockAuth("user");
        mockProjectAndApplicant(1L, 10L);
        when(service.createTeam(anyLong(), anyLong(), any())).thenAnswer(invocation -> {
            throw new RuntimeException("Error");
        });

        CreateTeamRequest req = new CreateTeamRequest(1L, "title", "desc", Collections.emptyList(), Collections.emptyList());
        ResponseEntity<String> response = controller.createTeam(1L, req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error", response.getBody());
    }

    @Test
    public void testDecideFailure() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1L);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1L, 10L);
        doAnswer(invocation -> {
            throw new RuntimeException("Decision Error");
        }).when(service).decideApplication(anyLong(), anyLong(), anyLong(), any());

        ResponseEntity<String> response = controller.decide(100L, 200L, new DecisionRequest("ACCEPT", null, null));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Decision Error", response.getBody());
    }

    @Test
    public void testApply() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1L);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1L, 10L);

        ResponseEntity<Void> response = controller.apply(100L);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(service).applyToTeam(100L, 10L);
    }

    @Test
    public void testDecide() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1L);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1L, 10L);

        ResponseEntity<String> response = controller.decide(100L, 200L, new DecisionRequest("ACCEPT", null, null));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testGetTeamApplications() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1L);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1L, 10L);
        when(service.getTeamApplications(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        ResponseEntity<List<TeamApplicationDTO>> response = controller.getTeamApplications(100L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testKick() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1L);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1L, 10L);

        ResponseEntity<Void> response = controller.kick(100L, 500L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testLeave() {
        mockAuth("user");
        Project project = new Project();
        project.setProjectId(1L);
        Team team = new Team();
        team.setProject(project);
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        mockProjectAndApplicant(1L, 10L);

        ResponseEntity<Void> response = controller.leave(100L, 500L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testList() {
        when(service.listTeams(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<TeamListingDTO>> response = controller.list(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
