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

    @Mock private ApplicantRepository applicantRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ProjectRepository projectRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    public void testCreateTeamsProjectNotFound() {
        when(projectRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.createTeams(1));
    }

    @Test
    public void testCreateTeamsSuccess() {
        Integer projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setMinNrParticipants(2);
        project.setMaxNrParticipants(4);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(teamRepository.incompleteTeamsByProject(projectId, 2)).thenReturn(new ArrayList<>());

        Applicant a1 = new Applicant();
        a1.setApplicantId(10);
        Applicant a2 = new Applicant();
        a2.setApplicantId(11);
        Applicant a3 = new Applicant();
        a3.setApplicantId(12);
        List<Applicant> singleApplicants = new ArrayList<>(List.of(a1, a2, a3));
        when(applicantRepository.getSingleApplicants(projectId)).thenReturn(singleApplicants);
        when(teamRepository.save(any(Team.class))).thenAnswer(i -> i.getArgument(0));

        teamService.createTeams(projectId);

        verify(teamRepository, atLeastOnce()).save(any(Team.class));
        verify(applicantRepository, atLeast(3)).save(any(Applicant.class));
        assertNotNull(a1.getTeam());
        assertEquals(a1.getTeam(), a2.getTeam());
        assertEquals(a1.getTeam(), a3.getTeam());
    }

    @Test
    public void testCreateTeamsWithIncompleteTeams() {
        Integer projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setMinNrParticipants(2);
        project.setMaxNrParticipants(3);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Team t1 = new Team();
        t1.setTeamId(100);
        TeamNrMembers tnm1 = new TeamNrMembers(t1, 1);
        List<TeamNrMembers> incomplete = new ArrayList<>(List.of(tnm1));
        when(teamRepository.incompleteTeamsByProject(projectId, 2)).thenReturn(incomplete);

        Applicant a1 = new Applicant();
        a1.setApplicantId(10);
        Applicant a2 = new Applicant();
        a2.setApplicantId(11);
        when(applicantRepository.getSingleApplicants(projectId)).thenReturn(new ArrayList<>(List.of(a1, a2)));

        teamService.createTeams(projectId);

        verify(applicantRepository, times(2)).save(any(Applicant.class));
        assertEquals(t1, a1.getTeam());
        assertEquals(t1, a2.getTeam());
    }

    @Test
    public void testJoinTeamsLogic() {
        Team t1 = new Team();
        t1.setTeamId(1);
        Team t2 = new Team();
        t2.setTeamId(2);
        Team t3 = new Team();
        t3.setTeamId(3);

        List<TeamNrMembers> incomplete = new ArrayList<>();
        incomplete.add(new TeamNrMembers(t1, 1));
        incomplete.add(new TeamNrMembers(t2, 1));
        incomplete.add(new TeamNrMembers(t3, 3)); // 3 == maxNr-1 = 3, so removed

        teamService.joinTeams(incomplete, 4);

        verify(teamRepository).joinTeams(t1, t2);
        verify(teamRepository).delete(t2);
    }
}