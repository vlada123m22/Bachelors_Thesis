package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Team;
import com.example.timesaver.model.dto.applicants.display.*;
import com.example.timesaver.model.dto.application.TeammateDTO;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.QuestionAnswerRepository;
import com.example.timesaver.repository.QuestionRepository;
import com.example.timesaver.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicantsDisplayService {

    private final TeamRepository teamRepository;
    private final ApplicantRepository applicantRepository;
    private final QuestionRepository questionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;

    public ApplicantsDisplayService(TeamRepository teamRepository, ApplicantRepository applicantRepository, QuestionRepository questionRepository, QuestionAnswerRepository questionAnswerRepository) {
        this.teamRepository = teamRepository;
        this.applicantRepository = applicantRepository;
        this.questionRepository = questionRepository;
        this.questionAnswerRepository = questionAnswerRepository;
    }

    public GetTeamsDTO getTeams(Integer projectId){

        List<Team> teams = teamRepository.findAllTeamsByProject(projectId);
        List<TeammateDTO> singleApplicants = applicantRepository.getSingleApplicantsFirstAndLastName(projectId);

        List<TeamDTO> teamDTOs = new ArrayList<>();

        for (Team team : teams) {
            String teamName = team.getTeamName();
            List<TeammateDTO> teamMembers = applicantRepository.getFirstAndLastNameByTeam(projectId, team.getTeamId());
            TeamDTO teamDTO = new TeamDTO(teamName, teamMembers);
             if (!teamDTO.getTeamMembers().isEmpty()) teamDTOs.add(teamDTO);
        }

        return new GetTeamsDTO(teamDTOs, singleApplicants);


    }

    public GetParticipantsDTO getParticipants(Integer projectId){
        List<QuestionDTO> questionDTOs = questionRepository.findQuestionByProjectId(projectId);

        List<GetParticipantsHelperDTO> participantsWithTeam = applicantRepository.getApplicantsWithTeam(projectId);
        List<GetParticipantsHelperDTO> participantsWithNoTeam = applicantRepository.getApplicantsWithNoTeam(projectId);

        List<ParticipantWithTeamDTO> participantsWithTeamDTOs = new ArrayList<>();
        List<ParticipantWithNoTeamDTO> participantsWithNoTeamDTOs = new ArrayList<>();

        for (GetParticipantsHelperDTO applicant : participantsWithTeam){
            List<QuestionAnswerDTO> questionAnswers = questionAnswerRepository.findQuestionNumberAndAnswerByApplicantId(applicant.getApplicantId());
            ParticipantWithTeamDTO participantDTO = new ParticipantWithTeamDTO(applicant.getFirstName(),applicant.getLastName(), applicant.getTeamName(), applicant.getIsSelected(), questionAnswers);
            participantsWithTeamDTOs.add(participantDTO);
        }

        for(GetParticipantsHelperDTO applicant : participantsWithNoTeam){
            List<QuestionAnswerDTO> questionAnswers = questionAnswerRepository.findQuestionNumberAndAnswerByApplicantId(applicant.getApplicantId());
            ParticipantWithNoTeamDTO participantDTO = new ParticipantWithNoTeamDTO(applicant.getFirstName(),applicant.getLastName(), applicant.getIsSelected(),questionAnswers);
            participantsWithNoTeamDTOs.add(participantDTO);
        }

        return new GetParticipantsDTO(questionDTOs, participantsWithTeamDTOs, participantsWithNoTeamDTOs);

    }
}
