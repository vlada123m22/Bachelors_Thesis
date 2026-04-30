package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.participant.ParticipantProjectStatusDTO;
import com.example.timesaver.model.dto.participant.TeamApplicationStatusDTO;
import com.example.timesaver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamApplicationRepository teamApplicationRepository;

    @Transactional(readOnly = true)
    public List<ParticipantProjectStatusDTO> getParticipantApplications(String userName) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        return applicantRepository.findProjectStatusByUser(user);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getTeamApplicationsForProject(String userName, Integer projectId) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();

        // Check if project exists
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        Project project = projectOpt.get();

        // Check if teams_preformed is false
        if (project.getTeamsPreformed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("This project has pre-formed teams. Team applications are not available.");
        }

        // Find the applicant record for this user and project
        Optional<Applicant> applicantOpt = applicantRepository.findByUserAndProject(user, project);
        if (applicantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("You have not applied to this project");
        }

        Applicant applicant = applicantOpt.get();

        // Get all team applications for this applicant
        List<TeamApplication> teamApplications = teamApplicationRepository.findAll().stream()
                .filter(ta -> ta.getApplicant().getApplicantId().equals(applicant.getApplicantId()))
                .toList();

        List<TeamApplicationStatusDTO> result = new ArrayList<>();
        for (TeamApplication ta : teamApplications) {
            result.add(new TeamApplicationStatusDTO(
                    ta.getTeam().getTeamId(),
                    ta.getTeam().getTeamName(),
                    ta.getStatus().toString(),
                    ta.getAppliedAt(),
                    ta.getDecisionAt()
            ));
        }

        return ResponseEntity.ok(result);
    }
}
