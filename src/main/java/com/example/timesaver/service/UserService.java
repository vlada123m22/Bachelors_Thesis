package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamMemberBackgroundRepository teamMemberBackgroundRepository;

    @Autowired
    private TeamMemberRoleRepository teamMemberRoleRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private TeamApplicationRepository teamApplicationRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public void deleteParticipantProfile(String userName) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Find all applicant records for this user
        List<Applicant> applicants = applicantRepository.findAll().stream()
                .filter(a -> a.getUser() != null && a.getUser().getId().equals(user.getId()))
                .toList();

        for (Applicant applicant : applicants) {
            // 1. Delete question answers associated with this applicant
            List<QuestionAnswer> answers = questionAnswerRepository.findAll().stream()
                    .filter(qa -> qa.getApplicant().getApplicantId().equals(applicant.getApplicantId()))
                    .toList();
            questionAnswerRepository.deleteAll(answers);

            // 2. Delete team applications
            List<TeamApplication> teamApps = teamApplicationRepository.findAll().stream()
                    .filter(ta -> ta.getApplicant().getApplicantId().equals(applicant.getApplicantId()))
                    .toList();
            teamApplicationRepository.deleteAll(teamApps);

            // 3. Find team member records
            List<TeamMember> teamMembers = teamMemberRepository.findAll().stream()
                    .filter(tm -> tm.getApplicant().getApplicantId().equals(applicant.getApplicantId()))
                    .toList();

            for (TeamMember teamMember : teamMembers) {
                // Delete team_member_backgrounds
                teamMemberBackgroundRepository.deleteByMemberTeamMemberId(teamMember.getTeamMemberId());

                // Delete team_member_roles
                teamMemberRoleRepository.deleteByMemberTeamMemberId(teamMember.getTeamMemberId());

                // Delete team_member
                teamMemberRepository.delete(teamMember);
            }

            // 4. Update teams if this applicant is a lead
            List<Team> leaderTeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLead() != null && t.getLead().getApplicantId().equals(applicant.getApplicantId()))
                    .toList();
            for (Team team : leaderTeams) {
                team.setLead(null);
                teamRepository.save(team);
            }

            // 5. Delete the applicant
            applicantRepository.delete(applicant);
        }

        // 6. Finally delete the user
        userRepository.delete(user);
    }

    @Transactional
    public void deleteOrganizerProfile(String userName) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Find all projects organized by this user
        List<Project> projects = projectRepository.findAll().stream()
                .filter(p -> p.getOrganizer().getId().equals(user.getId()))
                .toList();

        for (Project project : projects) {
            // 1. Delete all question answers for applicants of this project
            List<Applicant> projectApplicants = applicantRepository.findAll().stream()
                    .filter(a -> a.getProject().getProjectId().equals(project.getProjectId()))
                    .toList();

            for (Applicant applicant : projectApplicants) {
                List<QuestionAnswer> answers = questionAnswerRepository.findAll().stream()
                        .filter(qa -> qa.getApplicant().getApplicantId().equals(applicant.getApplicantId()))
                        .toList();
                questionAnswerRepository.deleteAll(answers);
            }

            // 2. Delete all teams and related data for this project
            List<Team> teams = teamRepository.findAllTeamsByProject(project.getProjectId());

            for (Team team : teams) {
                // Delete team applications for this team
                List<TeamApplication> teamApps = teamApplicationRepository.findByTeam(team);
                teamApplicationRepository.deleteAll(teamApps);

                // Delete team members and their backgrounds/roles
                List<TeamMember> teamMembers = teamMemberRepository.findByTeam(team);
                for (TeamMember teamMember : teamMembers) {
                    teamMemberBackgroundRepository.deleteByMemberTeamMemberId(teamMember.getTeamMemberId());
                    teamMemberRoleRepository.deleteByMemberTeamMemberId(teamMember.getTeamMemberId());
                }
                teamMemberRepository.deleteAll(teamMembers);

                // Delete team
                teamRepository.delete(team);
            }

            // 3. Delete all applicants for this project
            applicantRepository.deleteAll(projectApplicants);

            // 4. Delete all form questions for this project
            List<FormQuestion> questions = questionRepository.findAll().stream()
                    .filter(q -> q.getProject().getProjectId().equals(project.getProjectId()))
                    .toList();
            questionRepository.deleteAll(questions);

            // 5. Delete the project
            projectRepository.delete(project);
        }

        // 6. Finally delete the user
        userRepository.delete(user);
    }
}
