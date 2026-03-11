package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.project.*;
import com.example.timesaver.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Service
public class ProjectService {

    private static final String DEFAULT_ROLES_Q = "What are your roles in the project?";
    private static final String DEFAULT_BKG_Q = "What is the background of the project?";

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

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
            project.setTeamsPreformed(Objects.nonNull(request.getTeamsPreformed()) && request.getTeamsPreformed());
            project.setOrganizer(organizer);

        project.setRolesOptions(PipeList.join(request.getRoleOptions()));
        project.setBackgroundOptions(PipeList.join(request.getBackgroundOptions()));

        project.setRolesQuestionText(defaultIfBlank(request.getRolesQuestionText(), DEFAULT_ROLES_Q));
        project.setBackgroundQuestionText(defaultIfBlank(request.getBackgroundQuestionText(), DEFAULT_BKG_Q));



        Project savedProject = projectRepository.save(project);

            // Add form questions
            saveQuestions(request.getFormQuestions(), project);
            saveSchedules(request.getSchedules(), savedProject);


            return new ProjectResponse("Success", null, savedProject.getProjectId());

    }

    @Transactional(readOnly = true)
    public GetProjectResponse getProject(Long projectId, String userTimezone) {
        try {

            ZoneId zone = ZoneId.of(userTimezone);
            // Find project with questions
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                throw new RuntimeException("Project not found");
            }

            Project project = projectOpt.get();

            // Check if user is the organizer (only organizer can view/edit) - removed because everybody should be able to view any project. Only edit permitions should be limited to organizer.
//            if (!project.getOrganizer().getId().equals(currentUser.getId())) {
//                throw new RuntimeException("You don't have permission to view this project");
//            }

            // Convert to DTO
            List<FormQuestionDTO> questionDTOs = questionRepository.findByProjectId(projectId).stream()
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
    public ResponseEntity<ProjectResponse> editProject(EditProjectRequest request) {
        ProjectResponse responseBody;


            // Get current authenticated user
            User organizer = getCurrentUser();
            if (organizer == null) {
                responseBody = new ProjectResponse("Failure", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            }

            // Find existing project
            Optional<Project> projectOpt = projectRepository.findById(request.getProjectId());
            if (projectOpt.isEmpty()) {
                responseBody = new ProjectResponse("Failure", "Project not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            }

            Project project = projectOpt.get();

            // Check if user is the organizer
            if (!project.getOrganizer().getId().equals(organizer.getId())) {
                responseBody = new ProjectResponse("Failure", "You don't have permission to edit this project");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
            }

            // Validate participant numbers
            if (request.getMinNrParticipants() != null && request.getMaxNrParticipants() != null) {
                if (request.getMinNrParticipants() > request.getMaxNrParticipants()) {

                    responseBody = new ProjectResponse("Failure", "Minimum participants cannot exceed maximum participants");
                    return ResponseEntity.badRequest().body(responseBody);
                }
            }

            // Validate question numbers
            if (!validateQuestionNumbers(request.getFormQuestions())) {

                responseBody = new ProjectResponse("Failure", "Question numbers must be unique and start from 1");
                return ResponseEntity.badRequest().body(responseBody);
            }

            // Update project details
            project.setProjectName(request.getProjectName());
            project.setMaxNrParticipants(request.getMaxNrParticipants());
            project.setMinNrParticipants(request.getMinNrParticipants());
            project.setProjectDescription(request.getProjectDescription());
            project.setStartDate(request.getStartDate());
            project.setEndDate(request.getEndDate());

        if (request.getRoleOptions() != null) {
            project.setRolesOptions(PipeList.join(request.getRoleOptions()));
        }
        if (request.getBackgroundOptions() != null) {
            project.setBackgroundOptions(PipeList.join(request.getBackgroundOptions()));
        }
        if (request.getRolesQuestionText() != null) {
            project.setRolesQuestionText(defaultIfBlank(request.getRolesQuestionText(), DEFAULT_ROLES_Q));
        }
        if (request.getBackgroundQuestionText() != null) {
            project.setBackgroundQuestionText(defaultIfBlank(request.getBackgroundQuestionText(), DEFAULT_BKG_Q));
        }


        projectRepository.save(project);



            questionRepository.deleteQuestions(project.getProjectId());
            saveQuestions(request.getFormQuestions(), project);

        // Clear old questions and save new ones
        questionRepository.deleteQuestions(project.getProjectId());
        saveQuestions(request.getFormQuestions(), project);
        
        scheduleRepository.deleteAll(scheduleRepository.findByProjectProjectId(project.getProjectId()));
        saveSchedules(request.getSchedules(), project);
            responseBody = new ProjectResponse("Success", null, project.getProjectId());
            return ResponseEntity.ok().body(responseBody);
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
                        List<FormQuestionDTO> questionDTOs = questionRepository.findByProjectId(project.getProjectId()).stream()
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

    public User getCurrentUser() {
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

    private void saveQuestions(List<FormQuestionDTO> dtos, Project project) {
        for (FormQuestionDTO dto : dtos) {
            FormQuestion question = new FormQuestion();
            question.setQuestionNumber(dto.getQuestionNumber());
            question.setQuestionType(dto.getQuestionType());
            question.setQuestion(dto.getQuestion());
            question.setCheckboxOptions(dto.getCheckboxOptions());
            question.setProject(project);
            questionRepository.save(question);

        }
    }

    private FormQuestionDTO convertToDTO(FormQuestion question) {
        FormQuestionDTO dto = new FormQuestionDTO();
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestion(question.getQuestion());
        dto.setCheckboxOptions(question.getCheckboxOptions());
        return dto;
    }

    // Logic to check if a user can see the schedule
    public boolean canUserViewSchedule(Project project, User user) {
        ScheduleVisibility visibility = project.getScheduleVisibility();
        if (visibility == ScheduleVisibility.EVERYBODY) return true;
        if (user == null) return false;
        if (project.getOrganizer().getId().equals(user.getId())) return true;

        Optional<Applicant> applicantOpt = applicantRepository.findByUserAndProject(user, project);
        if (applicantOpt.isEmpty()) return false;

        if (visibility == ScheduleVisibility.APPLICANTS) return true;
        if (visibility == ScheduleVisibility.ACCEPTED_PARTICIPANTS) {
            return applicantOpt.get().getIsSelected();
        }
        return false;
    }


    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public List<ScheduleDTO> getScheduleByDay(Long projectId, Integer dayNumber) {
        List<ProjectSchedule> projectScheduleList = scheduleRepository.findByProjectProjectIdAndDayNumber(projectId, dayNumber);
        List<ScheduleDTO> scheduleDTOs = new ArrayList<>();
        for(ProjectSchedule schedule : projectScheduleList){
            ScheduleDTO scheduleItem = new ScheduleDTO();
            scheduleItem.setActivityTitle(schedule.getActivityTitle());
            scheduleItem.setActivityDescription(schedule.getActivityDescription());
            scheduleItem.setStartTime(schedule.getStartTime());
            scheduleItem.setEndTime(schedule.getEndTime());
            scheduleDTOs.add(scheduleItem);
        }

        return scheduleDTOs;
    }

    private void saveSchedules(List<ScheduleDTO> dtos, Project project) {
        if (dtos == null || dtos.isEmpty()) return;

        List<ProjectSchedule> schedules = dtos.stream().map(dto -> {
            ProjectSchedule schedule = new ProjectSchedule();
            schedule.setProject(project);
            schedule.setDayNumber(dto.getDayNumber());
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            schedule.setActivityTitle(dto.getActivityTitle());
            schedule.setActivityDescription(dto.getActivityDescription());
            return schedule;
        }).collect(Collectors.toList());

        scheduleRepository.saveAll(schedules);
    }
}