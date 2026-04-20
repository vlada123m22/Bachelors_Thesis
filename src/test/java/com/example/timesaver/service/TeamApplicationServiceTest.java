package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.teamflow.CreateTeamRequest;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamApplicationDTO;
import com.example.timesaver.model.dto.teamflow.TeamListingDTO;
import com.example.timesaver.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamApplicationServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamApplicationRepository teamApplicationRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private TeamRoleRequirementRepository roleReqRepo;
    @Mock private TeamBackgroundRequirementRepository bgReqRepo;
    @Mock private TeamMemberRoleRepository memberRoleRepo;
    @Mock private TeamMemberBackgroundRepository memberBgRepo;

    @InjectMocks
    private TeamApplicationService teamApplicationService;

    @Test
    public void testCreateTeamSuccess() {
        Long projectId = 1L;
        Long leadId = 10L;
        Project p = new Project();
        p.setProjectId(projectId);
        p.setMaxNrParticipants(5);
        p.setRolesOptions("Dev|Design");
        p.setBackgroundOptions("CS|Art");
        p.setTeamsPreformed(false);

        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        lead.setProject(p);
        lead.setTeam(null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));
        when(applicantRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(Collections.emptyList());
        when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTeamRequest req = new CreateTeamRequest(projectId, "Title", "Desc", 
            List.of(new CreateTeamRequest.RoleReq("Dev", 1, 2)), 
            List.of(new CreateTeamRequest.BackgroundReq("CS", 1, 1)));
        Team result = teamApplicationService.createTeam(projectId, leadId, req);

        assertNotNull(result);
        assertEquals("Title", result.getIdeaTitle());
        assertEquals(lead, result.getLead());
        verify(roleReqRepo).save(any());
        verify(bgReqRepo).save(any());
    }

    @Test
    public void testCreateTeamLeadAlreadyInTeam() {
        Long projectId = 1L;
        Project p = new Project();
        p.setProjectId(projectId);
        p.setTeamsPreformed(false);

        Applicant lead = new Applicant();
        lead.setApplicantId(10L);
        lead.setProject(p);

        Team existing = new Team();
        existing.setLead(lead);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));
        when(applicantRepository.findById(10L)).thenReturn(Optional.of(lead));
        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () -> 
            teamApplicationService.createTeam(projectId, 10L, new CreateTeamRequest(projectId, "T", "D", null, null)));
    }

    @Test
    public void testApplyToTeamSuccess() {
        Long teamId = 1L;
        Long applicantId = 2L;
        Project p = new Project();
        p.setProjectId(100L);
        p.setTeamsPreformed(false);
        Team team = new Team();
        team.setTeamId(teamId);
        team.setProject(p);
        
        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setProject(p);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
        when(teamRepository.findAllTeamsByProject(100L)).thenReturn(List.of(team));

        teamApplicationService.applyToTeam(teamId, applicantId);
        verify(teamApplicationRepository).save(any());
    }

    @Test
    public void testDecideApplicationAccept() {
        Long teamId = 1L;
        Long leadId = 5L;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);
        Project p = new Project();
        p.setMaxNrParticipants(5);
        team.setProject(p);

        TeamApplication app = new TeamApplication();
        app.setId(100L);
        app.setTeam(team);
        app.setStatus(TeamApplication.Status.PENDING);
        Applicant applicant = new Applicant();
        app.setApplicant(applicant);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(teamMemberRepository.countByTeam(team)).thenReturn(1L);

        DecisionRequest req = new DecisionRequest("ACCEPT", List.of("Dev"), null);
        
        TeamRoleRequirement trr = new TeamRoleRequirement();
        trr.setRoleCode("Dev");
        trr.setMaxNeeded(2);
        when(roleReqRepo.findByTeam(team)).thenReturn(List.of(trr));

        teamApplicationService.decideApplication(teamId, 100L, leadId, req);

        assertEquals(TeamApplication.Status.ACCEPTED, app.getStatus());
        verify(teamMemberRepository).save(any());
        verify(teamApplicationRepository).save(app);
    }

    @Test
    public void testApplyToTeam() {
        Long teamId = 1L;
        Long applicantId = 2L;
        Team team = new Team();
        Project p = new Project();
        p.setMaxNrParticipants(5);
        p.setTeamsPreformed(false);
        team.setProject(p);
        
        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setTeam(null);
        applicant.setProject(p);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));

        teamApplicationService.applyToTeam(teamId, applicantId);
        verify(teamApplicationRepository).save(any());
    }

    @Test
    public void testListTeams() {
        Long projectId = 1L;
        Project p = new Project();
        p.setProjectId(projectId);
        Team team = new Team();
        team.setTeamId(100L);
        team.setProject(p);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));
        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(List.of(team));
        when(teamMemberRepository.countByTeam(team)).thenReturn(1L);

        List<TeamListingDTO> result = teamApplicationService.listTeams(projectId);
        assertEquals(1, result.size());
    }

    @Test
    public void testLeaveTeam() {
        Long teamId = 1L;
        Long memberId = 10L;
        Team team = new Team();
        team.setTeamId(teamId);
        TeamMember member = new TeamMember();
        member.setId(memberId);
        member.setTeam(team);
        Applicant applicant = new Applicant();
        applicant.setApplicantId(memberId);
        member.setApplicant(applicant);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        teamApplicationService.leaveTeam(teamId, memberId, memberId);
        verify(teamMemberRepository).delete(member);
    }

    @Test
    public void testRemoveMember() {
        Long teamId = 1L;
        Long memberId = 10L;
        Long leadId = 5L;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);

        TeamMember member = new TeamMember();
        member.setId(memberId);
        member.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        teamApplicationService.removeMember(teamId, memberId, leadId);
        verify(teamMemberRepository).delete(member);
    }

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
