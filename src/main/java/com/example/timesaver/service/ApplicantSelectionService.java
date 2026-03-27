package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicantSelectionService {
    private final ApplicantRepository applicantRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ApplicantSelectionService(ApplicantRepository applicantRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.applicantRepository = applicantRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String username = auth.getName();
        return userRepository.findByUserName(username).orElse(null);
    }

    private Project requireOrganizerOfProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User user = getCurrentUser();
        if (user == null || !project.getOrganizer().getId().equals(user.getId())) {
            throw new SecurityException("Only the organizer can modify selections for this project");
        }
        return project;
    }

    @Transactional
    public void setApplicantSelection(Long projectId, Long applicantId, boolean selected) {
        Project project = requireOrganizerOfProject(projectId);
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("Applicant not found"));
        if (!applicant.getProject().getProjectId().equals(project.getProjectId())) {
            throw new IllegalArgumentException("Applicant does not belong to this project");
        }
        applicant.setIsSelected(selected);
        applicantRepository.save(applicant);
    }

    @Transactional
    public int bulkSetSelection(Long projectId, List<Long> applicantIds, boolean selected) {
        requireOrganizerOfProject(projectId);
        List<Applicant> toUpdate = applicantRepository.findAllById(applicantIds);
        // ensure all belong to the same project
        for (Applicant a : toUpdate) {
            if (!a.getProject().getProjectId().equals(projectId)) {
                throw new IllegalArgumentException("One or more applicants do not belong to this project");
            }
            a.setIsSelected(selected);
        }
        applicantRepository.saveAll(toUpdate);
        return toUpdate.size();
    }
}