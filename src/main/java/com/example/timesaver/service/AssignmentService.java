package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.AssignmentRepository;
import com.example.timesaver.repository.SubmissionRepository;
import com.example.timesaver.repository.UserRepository;
import com.example.timesaver.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AssignmentService {
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicantRepository applicantRepository;


    public Submission submitAssignment(Long assignmentId, String username, String text, MultipartFile file) throws IOException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Find the applicant record for this user and project to get their team
        Applicant applicant = applicantRepository.findByUserAndProject(user, assignment.getProject())
                .orElseThrow(() -> new RuntimeException("User is not a participant in this project"));

        Team team = applicant.getTeam();
        if (team == null) {
            throw new RuntimeException("User is not part of a team");
        }

        Submission submission = submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, team.getTeamId())
                .orElse(new Submission());

        submission.setAssignment(assignment);
        submission.setTeam(team);
        submission.setUploadedBy(user);
        submission.setTextContent(text);

        if (file != null && !file.isEmpty()) {
            String path = fileStorageService.storeFile(file, team.getProject().getProjectId(), user.getId(), 0);
            submission.setFilePath(path);
        }

        return submissionRepository.save(submission);
    }


    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAssignmentsByProject(Long projectId) {
        return assignmentRepository.findByProjectProjectId(projectId);
    }

    public Submission submitAssignment(Long assignmentId, Team team, User user, String text, MultipartFile file) throws IOException {
        Submission submission = submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, team.getTeamId())
                .orElse(new Submission());

        submission.setAssignment(assignmentRepository.getReferenceById(assignmentId));
        submission.setTeam(team);
        submission.setUploadedBy(user);
        submission.setTextContent(text);

        if (file != null && !file.isEmpty()) {
            String path = fileStorageService.storeFile(file, team.getProject().getProjectId(), user.getId(), 0);
            submission.setFilePath(path);
        }

        return submissionRepository.save(submission);
    }

    public Submission getTeamSubmission(Long assignmentId, String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // For mentors/organizers, you might want to adjust logic to specify which team's submission to see.
        // This implementation assumes the caller is a participant looking for their own team's work.
        Applicant applicant = applicantRepository.findByUserAndProject(user, assignment.getProject())
                .orElseThrow(() -> new RuntimeException("User is not a participant in this project"));

        if (applicant.getTeam() == null) {
            throw new RuntimeException("User is not part of a team");
        }

        return submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, applicant.getTeam().getTeamId())
                .orElseThrow(() -> new RuntimeException("No submission found for this team"));
    }


    public List<Submission> getAllSubmissionsForAssignment(Long assignmentId) {
        // Check if assignment exists
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new RuntimeException("Assignment not found");
        }
        return submissionRepository.findByAssignmentId(assignmentId);
    }


}