package com.example.timesaver.service;

import com.example.timesaver.model.Team;
import com.example.timesaver.model.dto.applicants.display.*;
import com.example.timesaver.model.dto.application.TeammateDTO;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.QuestionAnswerRepository;
import com.example.timesaver.repository.QuestionRepository;
import com.example.timesaver.repository.TeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantsDisplayServiceTest {

    @Mock private TeamRepository teamRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionAnswerRepository questionAnswerRepository;

    @InjectMocks
    private ApplicantsDisplayService applicantsDisplayService;

    @Test
    public void testGetTeams() {
        Integer projectId = 1;
        Team team = new Team();
        team.setTeamId(10);
        team.setTeamName("Team A");

        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(List.of(team));
        when(applicantRepository.getSingleApplicantsFirstAndLastName(projectId))
                .thenReturn(List.of(new TeammateDTO("John", "Doe")));
        when(applicantRepository.getFirstAndLastNameByTeam(projectId, 10))
                .thenReturn(List.of(new TeammateDTO("Alice", "Smith")));

        GetTeamsDTO result = applicantsDisplayService.getTeams(projectId);

        assertEquals(1, result.getTeams().size());
        assertEquals("Team A", result.getTeams().get(0).getTeamName());
        assertEquals(1, result.getSingleParticipants().size());
        assertEquals("John", result.getSingleParticipants().get(0).getFirstName());
    }

    @Test
    public void testGetTeamsEmpty() {
        Integer projectId = 1;
        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(Collections.emptyList());
        when(applicantRepository.getSingleApplicantsFirstAndLastName(projectId)).thenReturn(Collections.emptyList());

        GetTeamsDTO result = applicantsDisplayService.getTeams(projectId);

        assertTrue(result.getTeams().isEmpty());
        assertTrue(result.getSingleParticipants().isEmpty());
    }

    @Test
    public void testGetParticipants() {
        Integer projectId = 1;
        QuestionDTO qDto = new QuestionDTO(1, "Question?");
        when(questionRepository.findQuestionByProjectId(projectId)).thenReturn(List.of(qDto));

        // getApplicantsWithTeam returns GetParticipantsHelperDTO
        GetParticipantsHelperDTO h1 = new GetParticipantsHelperDTO(100, "John", "Doe", "Team A", true);
        GetParticipantsHelperDTO h2 = new GetParticipantsHelperDTO(101, "Jane", "Smith", null, false);

        when(applicantRepository.getApplicantsWithTeam(projectId)).thenReturn(List.of(h1));
        when(applicantRepository.getApplicantsWithNoTeam(projectId)).thenReturn(List.of(h2));

        com.example.timesaver.model.dto.applicants.display.QuestionAnswerDTO qa1 =
                new com.example.timesaver.model.dto.applicants.display.QuestionAnswerDTO(1, "Answer 1");
        when(questionAnswerRepository.findQuestionNumberAndAnswerByApplicantId(100)).thenReturn(List.of(qa1));
        when(questionAnswerRepository.findQuestionNumberAndAnswerByApplicantId(101)).thenReturn(Collections.emptyList());

        GetParticipantsDTO result = applicantsDisplayService.getParticipants(projectId);

        assertEquals(1, result.getQuestionDTOs().size());
        assertEquals(1, result.getParticipantsWithTeams().size());
        assertEquals("John", result.getParticipantsWithTeams().get(0).getFirstName());
        assertEquals("Team A", result.getParticipantsWithTeams().get(0).getTeamName());
        assertEquals(1, result.getParticipantsWithoutTeams().size());
        assertEquals("Jane", result.getParticipantsWithoutTeams().get(0).getFirstName());
    }

    @Test
    @DisplayName("Should handle team with no members (filter out)")
    void testGetTeamsWithEmptyMemberTeam() {
        Integer projectId = 1;
        Team team = new Team();
        team.setTeamId(10);
        team.setTeamName("Empty Team");

        when(teamRepository.findAllTeamsByProject(projectId)).thenReturn(List.of(team));
        when(applicantRepository.getSingleApplicantsFirstAndLastName(projectId)).thenReturn(Collections.emptyList());
        when(applicantRepository.getFirstAndLastNameByTeam(projectId, 10)).thenReturn(Collections.emptyList());

        GetTeamsDTO result = applicantsDisplayService.getTeams(projectId);

        assertTrue(result.getTeams().isEmpty());
    }
}