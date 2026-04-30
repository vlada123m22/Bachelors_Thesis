package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.teamflow.CreateTeamRequest;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamApplicationDTO;
import com.example.timesaver.model.dto.teamflow.TeamListingDTO;
import com.example.timesaver.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

//    @Test
//    public void testCreateTeamSuccess() {
//        Integer projectId = 1;
//        Integer leadId = 10;
//        Project p = new Project();
//        p.setProjectId(projectId);
//        p.setMaxNrParticipants(5);
//        p.setRolesOptions("Dev|Design");
//        p.setBackgroundOptions("CS|Art");
//        p.setTeamsPreformed(false);
//
//        Applicant lead = new Applicant();
//        lead.setApplicantId(leadId);
//        lead.setProject(p);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));
//        when(applicantRepository.findById(leadId)).thenReturn(Optional.of(lead));
//        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(Collections.emptyList());
//        when(teamMemberRepository.findByTeamAndApplicant(any(), eq(lead))).thenReturn(Optional.empty());
//        when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//        CreateTeamRequest req = new CreateTeamRequest(projectId, "Title", "Desc",
//                List.of(new CreateTeamRequest.RoleReq("Dev", 1, 2)),
//                List.of(new CreateTeamRequest.BackgroundReq("CS", 1, 1)));
//        Team result = teamApplicationService.createTeam(projectId, leadId, req);
//
//        assertNotNull(result);
//        assertEquals("Title", result.getIdeaTitle());
//        assertEquals(lead, result.getLead());
//        verify(roleReqRepo).save(any());
//        verify(bgReqRepo).save(any());
//    }

    @Test
    public void testCreateTeamLeadAlreadyInTeam() {
        Integer projectId = 1;
        Project p = new Project();
        p.setProjectId(projectId);
        p.setTeamsPreformed(false);

        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        lead.setProject(p);

        Team existing = new Team();
        existing.setLead(lead);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));
        when(applicantRepository.findById(10)).thenReturn(Optional.of(lead));
        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () ->
                teamApplicationService.createTeam(projectId, 10, new CreateTeamRequest(projectId, "T", "D", null, null)));
    }

    @Test
    public void testApplyToTeamSuccess() {
        Integer teamId = 1;
        Integer applicantId = 2;
        Project p = new Project();
        p.setProjectId(100);
        p.setTeamsPreformed(false);
        Team team = new Team();
        team.setTeamId(teamId);
        team.setProject(p);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setProject(p);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
        when(teamRepository.findAllTeamsByProject(100)).thenReturn(List.of(team));
        when(teamMemberRepository.findByTeamAndApplicant(eq(team), eq(applicant))).thenReturn(Optional.empty());
        when(teamApplicationRepository.findByTeamAndApplicant(team, applicant)).thenReturn(Optional.empty());

        teamApplicationService.applyToTeam(teamId, applicantId);
        verify(teamApplicationRepository).save(any());
    }

    @Test
    public void testDecideApplicationAccept() {
        Integer teamId = 1;
        Integer leadId = 5;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);
        Project p = new Project();
        p.setMaxNrParticipants(5);
        team.setProject(p);

        TeamApplication app = new TeamApplication();
        app.setTeamApplicationId(100);
        app.setTeam(team);
        app.setStatus(TeamApplication.Status.PENDING);
        Applicant applicant = new Applicant();
        app.setApplicant(applicant);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(100)).thenReturn(Optional.of(app));
        when(teamMemberRepository.countByTeam(team)).thenReturn(1L);

        DecisionRequest req = new DecisionRequest("ACCEPT", List.of("Dev"), null);

        TeamRoleRequirement trr = new TeamRoleRequirement();
        trr.setRoleCode("Dev");
        trr.setMaxNeeded(2);
        when(roleReqRepo.findByTeam(team)).thenReturn(List.of(trr));
        when(memberRoleRepo.countByRole(team)).thenReturn(Collections.emptyList());
        when(memberBgRepo.countByBackground(team)).thenReturn(Collections.emptyList());
        when(bgReqRepo.findByTeam(team)).thenReturn(Collections.emptyList());
        when(teamMemberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        teamApplicationService.decideApplication(teamId, 100, leadId, req);

        assertEquals(TeamApplication.Status.ACCEPTED, app.getStatus());
        verify(teamMemberRepository).save(any());
        verify(teamApplicationRepository).save(app);
    }

    @Test
    public void testListTeams() {
        Integer projectId = 1;
        Project p = new Project();
        p.setProjectId(projectId);
        Team team = new Team();
        team.setTeamId(100);
        team.setProject(p);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));
        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(List.of(team));
        when(teamMemberRepository.countByTeam(team)).thenReturn(1L);
        when(roleReqRepo.findByTeam(team)).thenReturn(Collections.emptyList());
        when(bgReqRepo.findByTeam(team)).thenReturn(Collections.emptyList());
        when(memberRoleRepo.countByRole(team)).thenReturn(Collections.emptyList());
        when(memberBgRepo.countByBackground(team)).thenReturn(Collections.emptyList());

        List<TeamListingDTO> result = teamApplicationService.listTeams(projectId);
        assertEquals(1, result.size());
    }

    @Test
    public void testLeaveTeam() {
        Integer teamId = 1;
        Integer memberId = 10;
        Team team = new Team();
        team.setTeamId(teamId);
        TeamMember member = new TeamMember();
        member.setTeamMemberId(memberId);
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
        Integer teamId = 1;
        Integer memberId = 10;
        Integer leadId = 5;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);

        TeamMember member = new TeamMember();
        member.setTeamMemberId(memberId);
        member.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        teamApplicationService.removeMember(teamId, memberId, leadId);
        verify(teamMemberRepository).delete(member);
    }

    @Test
    public void testGetTeamApplicationsSuccess() {
        Integer teamId = 1;
        Integer leadId = 3;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(10);
        applicant.setFirstName("John");
        applicant.setLastName("Doe");

        TeamApplication app = new TeamApplication();
        app.setTeamApplicationId(100);
        app.setTeam(team);
        app.setApplicant(applicant);
        app.setStatus(TeamApplication.Status.PENDING);
        app.setAppliedAt(java.time.ZonedDateTime.now());

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findByTeam(team)).thenReturn(List.of(app));

        List<TeamApplicationDTO> result = teamApplicationService.getTeamApplications(teamId, leadId);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).id());
        assertEquals("John", result.get(0).firstName());
    }

    @Test
    public void testGetTeamApplicationsThrowsWhenNotLead() {
        Integer teamId = 1;
        Integer leadId = 3;
        Integer otherId = 4;
        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(leadId);
        team.setLead(lead);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThrows(RuntimeException.class, () ->
                teamApplicationService.getTeamApplications(teamId, otherId));
    }

    @Test
    public void testDecideApplicationThrowsNoSuchElementExceptionWhenAppNotFound() {
        Integer teamId = 1;
        Integer appId = 2;
        Integer actingLeadApplicantId = 3;
        DecisionRequest req = new DecisionRequest("ACCEPT", null, null);

        Team team = new Team();
        team.setTeamId(teamId);
        Applicant lead = new Applicant();
        lead.setApplicantId(actingLeadApplicantId);
        team.setLead(lead);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(appId)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () ->
                teamApplicationService.decideApplication(teamId, appId, actingLeadApplicantId, req));
    }

    @Test
    @DisplayName("Should decide application REJECT")
    void testDecideApplicationReject() {
        Team team = new Team();
        team.setTeamId(1);
        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        team.setLead(lead);

        TeamApplication app = new TeamApplication();
        app.setTeam(team);
        app.setStatus(TeamApplication.Status.PENDING);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(100)).thenReturn(Optional.of(app));

        DecisionRequest req = new DecisionRequest("REJECT", null, null);
        teamApplicationService.decideApplication(1, 100, 10, req);

        assertEquals(TeamApplication.Status.REJECTED, app.getStatus());
        verify(teamApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Should throw exception when non-lead decides application")
    void testDecideApplicationNotLead() {
        Team team = new Team();
        team.setTeamId(1);
        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        team.setLead(lead);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        DecisionRequest req = new DecisionRequest("ACCEPT", null, null);
        assertThrows(RuntimeException.class, () ->
                teamApplicationService.decideApplication(1, 100, 99, req));
    }

    @Test
    @DisplayName("Should throw exception when application team mismatch")
    void testDecideApplicationTeamMismatch() {
        Team team = new Team();
        team.setTeamId(1);
        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        team.setLead(lead);

        Team otherTeam = new Team();
        otherTeam.setTeamId(2);
        TeamApplication app = new TeamApplication();
        app.setTeam(otherTeam);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(100)).thenReturn(Optional.of(app));

        DecisionRequest req = new DecisionRequest("ACCEPT", null, null);
        assertThrows(RuntimeException.class, () ->
                teamApplicationService.decideApplication(1, 100, 10, req));
    }

    @Test
    @DisplayName("Should throw exception when application already decided")
    void testDecideApplicationAlreadyDecided() {
        Team team = new Team();
        team.setTeamId(1);
        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        team.setLead(lead);

        TeamApplication app = new TeamApplication();
        app.setTeam(team);
        app.setStatus(TeamApplication.Status.ACCEPTED);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(100)).thenReturn(Optional.of(app));

        DecisionRequest req = new DecisionRequest("ACCEPT", null, null);
        assertThrows(RuntimeException.class, () ->
                teamApplicationService.decideApplication(1, 100, 10, req));
    }

    @Test
    @DisplayName("Should throw exception when team is full")
    void testDecideApplicationTeamFull() {
        Team team = new Team();
        team.setTeamId(1);
        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        team.setLead(lead);
        Project p = new Project();
        p.setMaxNrParticipants(1); // Only lead fits (size = lead(1) + members(0) = 1, can't add more)
        team.setProject(p);

        TeamApplication app = new TeamApplication();
        app.setTeam(team);
        app.setStatus(TeamApplication.Status.PENDING);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(teamApplicationRepository.findById(100)).thenReturn(Optional.of(app));
        when(teamMemberRepository.countByTeam(team)).thenReturn(0L);

        DecisionRequest req = new DecisionRequest("ACCEPT", null, null);
        assertThrows(RuntimeException.class, () ->
                teamApplicationService.decideApplication(1, 100, 10, req));
    }

    @Test
    @DisplayName("Should fail to leave team if not member")
    void testLeaveTeamNotMember() {
        Team team = new Team();
        team.setTeamId(1);
        TeamMember member = new TeamMember();
        Team otherTeam = new Team();
        otherTeam.setTeamId(2);
        member.setTeam(otherTeam);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findById(50)).thenReturn(Optional.of(member));

        assertThrows(RuntimeException.class, () -> teamApplicationService.leaveTeam(1, 50, 10));
    }

    @Test
    @DisplayName("Should fail to remove member if not lead")
    void testRemoveMemberNotLead() {
        Team team = new Team();
        team.setTeamId(1);
        Applicant lead = new Applicant();
        lead.setApplicantId(10);
        team.setLead(lead);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        assertThrows(RuntimeException.class, () -> teamApplicationService.removeMember(1, 50, 99));
    }
}
