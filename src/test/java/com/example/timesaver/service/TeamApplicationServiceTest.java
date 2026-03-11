package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamApplicationDTO;
import com.example.timesaver.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamApplicationServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ApplicantRepository applicantRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamApplicationRepository teamApplicationRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private TeamRoleRequirementRepository roleReqRepo;
    @Mock
    private TeamBackgroundRequirementRepository bgReqRepo;
    @Mock
    private TeamMemberRoleRepository memberRoleRepo;
    @Mock
    private TeamMemberBackgroundRepository memberBgRepo;

    @InjectMocks
    private TeamApplicationService teamApplicationService;

    @Test
    public void testGetTeamApplicationsSuccess() {
        Long teamId = 1L;
        Long leadId = 3L;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(10L);
        applicant.setFirstName("John");
        applicant.setLastName("Doe");

        TeamApplication app = new TeamApplication();
        app.setId(100L);
        app.setTeam(team);
        app.setApplicant(applicant);
        app.setStatus(TeamApplication.Status.PENDING);
        app.setAppliedAt(java.time.ZonedDateTime.now());

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findByTeam(team)).thenReturn(List.of(app));

        List<TeamApplicationDTO> result = teamApplicationService.getTeamApplications(teamId, leadId);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).id());
        assertEquals("John", result.get(0).firstName());
    }

    @Test
    public void testGetTeamApplicationsThrowsWhenNotLead() {
        Long teamId = 1L;
        Long leadId = 3L;
        Long otherId = 4L;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThrows(RuntimeException.class, () -> {
            teamApplicationService.getTeamApplications(teamId, otherId);
        });
    }

    @Test
    public void testDecideApplicationThrowsNoSuchElementExceptionWhenAppNotFound() {
        Long teamId = 1L;
        Long appId = 2L;
        Long actingLeadApplicantId = 3L;
        DecisionRequest req = new DecisionRequest("ACCEPT", null, null);

        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(actingLeadApplicantId);
        team.setLead(lead);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(appId)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> {
            teamApplicationService.decideApplication(teamId, appId, actingLeadApplicantId, req);
        });
    }
}
