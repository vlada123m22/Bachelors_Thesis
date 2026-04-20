package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.Team;
import com.example.timesaver.model.dto.team.TeamNrMembers;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    public void testCreateTeamsProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.createTeams(1L));
    }

    @Test
    public void testCreateTeamsSuccess() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setMinNrParticipants(2);
        project.setMaxNrParticipants(4);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        // No incomplete teams initially
        when(teamRepository.incompleteTeamsByProject(projectId, 2)).thenReturn(new ArrayList<>());
        
        // Three single applicants to trigger new team creation and then addition to it
        Applicant a1 = new Applicant();
        a1.setApplicantId(10L);
        Applicant a2 = new Applicant();
        a2.setApplicantId(11L);
        Applicant a3 = new Applicant();
        a3.setApplicantId(12L);
        List<Applicant> singleApplicants = new ArrayList<>(List.of(a1, a2, a3));
        when(applicantRepository.getSingleApplicants(projectId)).thenReturn(singleApplicants);

        teamService.createTeams(projectId);

        // Should create a new team for first applicant, then add second and third applicant to it
        // Actually it might be 4 saves if one applicant is moved in second while loop
        verify(teamRepository, atLeastOnce()).save(any(Team.class));
        verify(applicantRepository, atLeast(3)).save(any(Applicant.class));
        assertNotNull(a1.getTeam());
        assertEquals(a1.getTeam(), a2.getTeam());
        assertEquals(a1.getTeam(), a3.getTeam());
    }

    @Test
    public void testCreateTeamsWithIncompleteTeams() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setMinNrParticipants(2);
        project.setMaxNrParticipants(3);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        Team t1 = new Team(); t1.setTeamId(100L);
        TeamNrMembers tnm1 = new TeamNrMembers(t1, 1L);
        List<TeamNrMembers> incomplete = new ArrayList<>(List.of(tnm1));
        when(teamRepository.incompleteTeamsByProject(projectId, 2)).thenReturn(incomplete);
        
        Applicant a1 = new Applicant(); a1.setApplicantId(10L);
        Applicant a2 = new Applicant(); a2.setApplicantId(11L);
        when(applicantRepository.getSingleApplicants(projectId)).thenReturn(new ArrayList<>(List.of(a1, a2)));

        teamService.createTeams(projectId);

        // a1 should be added to t1. t1 becomes full (size 2 < max 3, wait, size 1+1=2, if max=2 it would be full. If max=3 it still has 1 spot)
        // a2 should also be added to t1. t1 becomes full (size 3)
        verify(applicantRepository, times(2)).save(any(Applicant.class));
        assertEquals(t1, a1.getTeam());
        assertEquals(t1, a2.getTeam());
    }

    @Test
    public void testCreateTeamsMovableApplicants() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setMinNrParticipants(1);
        project.setMaxNrParticipants(4);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        Team t1 = new Team(); t1.setTeamId(100L);
        TeamNrMembers tnm1 = new TeamNrMembers(t1, 2L);
        
        // a1 is a single applicant
        Applicant a1 = new Applicant(); a1.setApplicantId(10L);
        
        when(applicantRepository.getSingleApplicants(projectId)).thenReturn(new ArrayList<>(List.of(a1)));
        when(teamRepository.incompleteTeamsByProject(projectId, 1)).thenReturn(new ArrayList<>(List.of(tnm1)));

        teamService.createTeams(projectId);
        
        assertEquals(t1, a1.getTeam());
        verify(applicantRepository, atLeast(2)).save(a1); 
    }

    @Test
    public void testJoinTeamsLogic() {
        Team t1 = new Team(); t1.setTeamId(1L);
        Team t2 = new Team(); t2.setTeamId(2L);
        Team t3 = new Team(); t3.setTeamId(3L);
        
        List<TeamNrMembers> incomplete = new ArrayList<>();
        incomplete.add(new TeamNrMembers(t1, 1L));
        incomplete.add(new TeamNrMembers(t2, 1L));
        incomplete.add(new TeamNrMembers(t3, 3L)); // Should be ignored if maxNr=4 because 3 == 4-1
        
        teamService.joinTeams(incomplete, 4);
        
        // t1 and t2 should be joined
        verify(teamRepository).joinTeams(t1, t2);
        verify(teamRepository).delete(t2);
    }
}
