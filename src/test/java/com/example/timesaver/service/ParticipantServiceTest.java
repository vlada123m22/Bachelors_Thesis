package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.TeamApplication;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.participant.ParticipantProjectStatusDTO;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.TeamApplicationRepository;
import com.example.timesaver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private TeamApplicationRepository teamApplicationRepository;

    @InjectMocks private ParticipantService participantService;

    @Test
    public void testGetParticipantApplicationsSuccess() {
        User user = new User();
        user.setId(1);
        List<ParticipantProjectStatusDTO> expected = Collections.emptyList();

        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(applicantRepository.findProjectStatusByUser(user)).thenReturn(expected);

        List<ParticipantProjectStatusDTO> result = participantService.getParticipantApplications("user");
        assertEquals(expected, result);
    }

    @Test
    public void testGetParticipantApplicationsUserNotFound() {
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> participantService.getParticipantApplications("user"));
    }

    @Test
    public void testGetTeamApplicationsUserNotFound() {
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsProjectNotFound() {
        User user = new User();
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(projectRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsTeamsPreformed() {
        User user = new User();
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));

        Project project = new Project();
        project.setTeamsPreformed(true);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsApplicantNotFound() {
        User user = new User();
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));

        Project project = new Project();
        project.setTeamsPreformed(false);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsSuccess() {
        User user = new User();
        user.setId(1);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));

        Project project = new Project();
        project.setProjectId(1);
        project.setTeamsPreformed(false);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        Applicant applicant = new Applicant();
        applicant.setApplicantId(10);
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));

        // No team applications for this applicant
        when(teamApplicationRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsReturnsApplications() {
        User user = new User();
        user.setId(1);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));

        Project project = new Project();
        project.setProjectId(1);
        project.setTeamsPreformed(false);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        Applicant applicant = new Applicant();
        applicant.setApplicantId(10);
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));

        com.example.timesaver.model.Team team = new com.example.timesaver.model.Team();
        team.setTeamId(5);
        team.setTeamName("Team A");

        TeamApplication ta = new TeamApplication();
        ta.setTeamApplicationId(1);
        ta.setApplicant(applicant);
        ta.setTeam(team);
        ta.setStatus(TeamApplication.Status.PENDING);
        ta.setAppliedAt(ZonedDateTime.now());

        when(teamApplicationRepository.findAll()).thenReturn(List.of(ta));

        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<?> body = (List<?>) resp.getBody();
        assertEquals(1, body.size());
    }

    @Test
    public void testGetTeamApplicationsForProjectNotTeamsPreformed() {
        // covers teamsPreformed == false branch where applicant IS found but has no matching team applications
        User user = new User(); user.setId(1);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        Project project = new Project();
        project.setProjectId(1);
        project.setTeamsPreformed(false);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        Applicant applicant = new Applicant(); applicant.setApplicantId(99);
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));

        // A team application belonging to a DIFFERENT applicant — should be filtered out
        Applicant other = new Applicant(); other.setApplicantId(55);
        TeamApplication ta = new TeamApplication();
        ta.setApplicant(other);
        com.example.timesaver.model.Team team = new com.example.timesaver.model.Team();
        team.setTeamId(1); team.setTeamName("T");
        ta.setTeam(team);
        ta.setStatus(TeamApplication.Status.PENDING);
        ta.setAppliedAt(java.time.ZonedDateTime.now());
        when(teamApplicationRepository.findAll()).thenReturn(List.of(ta));

        ResponseEntity<?> resp = participantService.getTeamApplicationsForProject("user", 1);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(0, ((List<?>) resp.getBody()).size());
    }


}