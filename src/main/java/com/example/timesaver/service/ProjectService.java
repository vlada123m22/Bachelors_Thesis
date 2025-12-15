package com.example.timesaver.service;

import com.example.timesaver.model.FormQuestion;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.project.*;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
            // Get current authenticated user
            User organizer = getCurrentUser();
            if (organizer == null) {
                return new ProjectResponse("Failure", "User not authenticated");
            }

            // Validate participant numbers
            if (request.getMinNrParticipants() != null && request.getMaxNrParticipants() != null) {
                if (request.getMinNrParticipants() > request.getMaxNrParticipants()) {
                    return new ProjectResponse("Failure", "Minimum participants cannot exceed maximum participants");
                }
            }

            // Validate question numbers are unique and sequential
            if (!validateQuestionNumbers(request.getFormQuestions())) {
                return new ProjectResponse("Failure", "Question numbers must be unique and start from 1");
            }

            // Create project
            Project project = new Project();
            project.setProjectName(request.getProjectName());
            project.setMaxNrParticipants(request.getMaxNrParticipants());
            project.setMinNrParticipants(request.getMinNrParticipants());
            project.setProjectDescription(request.getProjectDescription());
            project.setStartDate(request.getStartDate());
            project.setEndDate(request.getEndDate());
            project.setOrganizer(organizer);

            // Add form questions
            List<FormQuestion> questions = convertToFormQuestions(request.getFormQuestions(), project);
            project.setFormQuestionsWithBidirectional(questions);

            // Save project
            Project savedProject = projectRepository.save(project);

            return new ProjectResponse("Success", null, savedProject.getProjectId());

    }

    @Transactional(readOnly = true)
    public GetProjectResponse getProject(Long projectId, String userTimezone) {
        try {
            // Get current authenticated user - also delete - need this method to be public
//            User currentUser = getCurrentUser();
//            if (currentUser == null) {
//                throw new RuntimeException("User not authenticated");
//            }
            ZoneId zone = ZoneId.of(userTimezone);
            // Find project with questions
            Optional<Project> projectOpt = projectRepository.findByIdWithQuestions(projectId);
            if (projectOpt.isEmpty()) {
                throw new RuntimeException("Project not found");
            }

            Project project = projectOpt.get();

            // Check if user is the organizer (only organizer can view/edit) - removed because everybody should be able to view any project. Only edit permitions should be limited to organizer.
//            if (!project.getOrganizer().getId().equals(currentUser.getId())) {
//                throw new RuntimeException("You don't have permission to view this project");
//            }

            // Convert to DTO
            List<FormQuestionDTO> questionDTOs = project.getFormQuestions().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new GetProjectResponse(
                    project.getProjectId(),
                    project.getProjectName(),
                    project.getProjectDescription(),
                    (Objects.isNull(project.getStartDate()))?null:project.getStartDate().withZoneSameInstant(zone),
                    (Objects.isNull(project.getEndDate()))?null:project.getEndDate().withZoneSameInstant(zone),
                    project.getMaxNrParticipants(),
                    project.getMinNrParticipants(),
                    questionDTOs

            );

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public ProjectResponse editProject(EditProjectRequest request) {
        try {
            // Get current authenticated user
            User organizer = getCurrentUser();
            if (organizer == null) {
                return new ProjectResponse("Failure", "User not authenticated");
            }

            // Find existing project
            Optional<Project> projectOpt = projectRepository.findByIdWithQuestions(request.getProjectId());
            if (projectOpt.isEmpty()) {
                return new ProjectResponse("Failure", "Project not found");
            }

            Project project = projectOpt.get();

            // Check if user is the organizer
            if (!project.getOrganizer().getId().equals(organizer.getId())) {
                return new ProjectResponse("Failure", "You don't have permission to edit this project");
            }

            // Validate participant numbers
            if (request.getMinNrParticipants() != null && request.getMaxNrParticipants() != null) {
                if (request.getMinNrParticipants() > request.getMaxNrParticipants()) {
                    return new ProjectResponse("Failure", "Minimum participants cannot exceed maximum participants");
                }
            }

            // Validate question numbers
            if (!validateQuestionNumbers(request.getFormQuestions())) {
                return new ProjectResponse("Failure", "Question numbers must be unique and start from 1");
            }

            // Update project details
            project.setProjectName(request.getProjectName());
            project.setMaxNrParticipants(request.getMaxNrParticipants());
            project.setMinNrParticipants(request.getMinNrParticipants());
            project.setProjectDescription(request.getProjectDescription());
            project.setStartDate(request.getStartDate());
            project.setEndDate(request.getEndDate());

            // Clear existing questions and add new ones
            List<FormQuestion> questions = project.getFormQuestions();
            questions.clear();
            questions = convertToFormQuestions(request.getFormQuestions(), project);
            project.setFormQuestionsWithBidirectional(questions);

            // Save updated project
            projectRepository.save(project);

            return new ProjectResponse("Success", null, project.getProjectId());

        } catch (Exception e) {
            return new ProjectResponse("Failure", "An error occurred while updating the project: " + e.getMessage());
        }
    }

    @Transactional
    public ProjectResponse deleteProject(Long projectId) {
        try {
            // Get current authenticated user
            User organizer = getCurrentUser();
            if (organizer == null) {
                return new ProjectResponse("Failure", "User not authenticated");
            }

            // Check if project exists and belongs to organizer
            if (!projectRepository.existsByProjectIdAndOrganizer(projectId, organizer)) {
                return new ProjectResponse("Failure", "Project not found or you don't have permission to delete it");
            }

            // Delete project (cascade will delete form questions)
            projectRepository.deleteById(projectId);

            return new ProjectResponse("Success", "Project deleted successfully");

        } catch (Exception e) {
            return new ProjectResponse("Failure", "An error occurred while deleting the project: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<GetProjectResponse> getAllUserProjects() {
        try {
            // Get current authenticated user
            User organizer = getCurrentUser();
            if (organizer == null) {
                throw new RuntimeException("User not authenticated");
            }

            // Get all projects by organizer
            List<Project> projects = projectRepository.findByOrganizer(organizer);

            // Convert to DTOs
            return projects.stream()
                    .map(project -> {
                        List<FormQuestionDTO> questionDTOs = project.getFormQuestions().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());

                        return new GetProjectResponse(
                                project.getProjectId(),
                                project.getProjectName(),
                                project.getProjectDescription(),
                                project.getStartDate(),
                                project.getEndDate(),
                                project.getMaxNrParticipants(),
                                project.getMinNrParticipants(),
                                questionDTOs

                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Helper methods

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUserName(username).orElse(null);
    }

    private boolean validateQuestionNumbers(List<FormQuestionDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return true;
        }

        // Check for unique question numbers
        long distinctCount = questions.stream()
                .map(FormQuestionDTO::getQuestionNumber)
                .distinct()
                .count();

        if (distinctCount != questions.size()) {
            return false; // Duplicate question numbers
        }

        // Check if question numbers start from 1 and are sequential
        List<Integer> sortedNumbers = questions.stream()
                .map(FormQuestionDTO::getQuestionNumber)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < sortedNumbers.size(); i++) {
            if (sortedNumbers.get(i) != i + 1) {
                return false; // Not sequential starting from 1
            }
        }

        return true;
    }

    private List<FormQuestion> convertToFormQuestions(List<FormQuestionDTO> dtos, Project project) {
        List<FormQuestion> questions = new ArrayList<>();
        for (FormQuestionDTO dto : dtos) {
            FormQuestion question = new FormQuestion();
            question.setQuestionNumber(dto.getQuestionNumber());
            question.setQuestionType(dto.getQuestionType());
            question.setQuestion(dto.getQuestion());
            question.setCheckboxOptions(dto.getCheckboxOptions());
            question.setProject(project);
            questions.add(question);
        }
        return questions;
    }

    private FormQuestionDTO convertToDTO(FormQuestion question) {
        FormQuestionDTO dto = new FormQuestionDTO();
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestion(question.getQuestion());
        dto.setCheckboxOptions(question.getCheckboxOptions());
        return dto;
    }
}