package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.project.*;
import com.example.timesaver.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUser(String username, Long userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
    }

    @Test
    public void testCreateProjectSuccess() {
        mockUser("organizer", 1L);
        CreateProjectRequest req = new CreateProjectRequest();
        req.setProjectName("Test Project");
        req.setRoleOptions(List.of("Role1"));
        req.setBackgroundOptions(List.of("Bkg1"));
        req.setFormQuestions(Collections.emptyList());
        req.setSchedules(Collections.emptyList());
        req.setMinNrParticipants(1);
        req.setMaxNrParticipants(5);

        when(projectRepository.save(any())).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setProjectId(100L);
            return p;
        });

        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Success", resp.getStatus());
        assertEquals(100L, resp.getProjectId());
    }

    @Test
    public void testCreateProjectNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        CreateProjectRequest req = new CreateProjectRequest();
        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Failure", resp.getStatus());
        assertEquals("User not authenticated", resp.getMessage());
    }

    @Test
    public void testCreateProjectWithNullTeamsPreformed() {
        mockUser("organizer", 1L);
        CreateProjectRequest req = new CreateProjectRequest();
        req.setProjectName("Test Project");
        req.setTeamsPreformed(null);
        req.setFormQuestions(Collections.emptyList());
        req.setSchedules(Collections.emptyList());

        when(projectRepository.save(any())).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setProjectId(100L);
            return p;
        });

        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Success", resp.getStatus());
    }

    @Test
    public void testCreateProjectWithTeamsPreformedTrue() {
        mockUser("organizer", 1L);
        CreateProjectRequest req = new CreateProjectRequest();
        req.setProjectName("Test Project");
        req.setTeamsPreformed(true);
        req.setFormQuestions(Collections.emptyList());
        req.setSchedules(Collections.emptyList());

        when(projectRepository.save(any())).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setProjectId(100L);
            return p;
        });

        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Success", resp.getStatus());
    }

    @Test
    public void testCreateProjectWithQuestions() {
        mockUser("organizer", 1L);
        CreateProjectRequest req = new CreateProjectRequest();
        req.setProjectName("Test Project");
        FormQuestionDTO q1 = new FormQuestionDTO();
        q1.setQuestionNumber(1);
        q1.setQuestionType(QuestionType.TEXT);
        q1.setQuestion("Question 1");
        req.setFormQuestions(List.of(q1));
        req.setSchedules(Collections.emptyList());

        when(projectRepository.save(any())).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setProjectId(100L);
            return p;
        });

        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Success", resp.getStatus());
        verify(questionRepository, atLeastOnce()).save(any(FormQuestion.class));
    }

    @Test
    public void testCreateProjectWithSchedules() {
        mockUser("organizer", 1L);
        CreateProjectRequest req = new CreateProjectRequest();
        req.setProjectName("Test Project");
        req.setFormQuestions(Collections.emptyList());

        ScheduleDTO schedule = new ScheduleDTO();
        schedule.setDayNumber(1);
        schedule.setActivityTitle("Activity");
        req.setSchedules(List.of(schedule));

        when(projectRepository.save(any())).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setProjectId(100L);
            return p;
        });

        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Success", resp.getStatus());
        verify(scheduleRepository).saveAll(anyList());
    }

    @Test
    public void testCreateProjectValidationFailures() {
        mockUser("organizer", 1L);

        // Min > Max
        CreateProjectRequest req = new CreateProjectRequest();
        req.setMinNrParticipants(10);
        req.setMaxNrParticipants(5);
        ProjectResponse resp = projectService.createProject(req);
        assertEquals("Failure", resp.getStatus());
        assertEquals("Minimum participants cannot exceed maximum participants", resp.getMessage());

        // Non-sequential questions
        req = new CreateProjectRequest();
        FormQuestionDTO q1 = new FormQuestionDTO(); q1.setQuestionNumber(2);
        req.setFormQuestions(List.of(q1));
        resp = projectService.createProject(req);
        assertEquals("Failure", resp.getStatus());
        assertEquals("Question numbers must be unique and start from 1", resp.getMessage());

        // Duplicate question numbers
        req = new CreateProjectRequest();
        FormQuestionDTO q2 = new FormQuestionDTO(); q2.setQuestionNumber(1);
        FormQuestionDTO q3 = new FormQuestionDTO(); q3.setQuestionNumber(1);
        req.setFormQuestions(List.of(q2, q3));
        resp = projectService.createProject(req);
        assertEquals("Failure", resp.getStatus());
        assertEquals("Question numbers must be unique and start from 1", resp.getMessage());
    }

    @Test
    public void testGetProjectSuccess() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName("Test");
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(1));
        User organizer = new User(); organizer.setId(10L);
        project.setOrganizer(organizer);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(questionRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        GetProjectResponse resp = projectService.getProject(projectId, "UTC");
        assertEquals("Test", resp.getProjectName());
        assertNotNull(resp.getStartDate());
        assertNotNull(resp.getEndDate());
    }

    @Test
    public void testGetProjectWithNullDates() {
        Integer projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName("Test");
        project.setStartDate(null);
        project.setEndDate(null);
        User organizer = new User(); organizer.setId(10L);
        project.setOrganizer(organizer);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(questionRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        GetProjectResponse resp = projectService.getProject(projectId, "UTC");
        assertNull(resp.getStartDate());
        assertNull(resp.getEndDate());
    }

    @Test
    public void testGetProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> projectService.getProject(1L, "UTC"));
    }

    @Test
    public void testEditProjectSuccess() {
        mockUser("organizer", 1L);
        EditProjectRequest req = new EditProjectRequest();
        req.setProjectId(100L);
        req.setProjectName("Updated");
        req.setFormQuestions(Collections.emptyList());
        req.setSchedules(Collections.emptyList());

        User organizer = new User(); organizer.setId(1L);
        Project project = new Project();
        project.setProjectId(100);
        project.setOrganizer(organizer);

        when(projectRepository.findById(100)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(project);
        when(scheduleRepository.findByProjectProjectId(100)).thenReturn(Collections.emptyList());

        ResponseEntity<ProjectResponse> resp = projectService.editProject(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Success", resp.getBody().getStatus());
    }

    @Test
    public void testEditProjectNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        EditProjectRequest req = new EditProjectRequest();
        ResponseEntity<ProjectResponse> resp = projectService.editProject(req);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    public void testEditProjectNotFound() {
        mockUser("organizer", 1L);
        EditProjectRequest req = new EditProjectRequest();
        req.setProjectId(100L);

        when(projectRepository.findById(100L)).thenReturn(Optional.empty());

        ResponseEntity<ProjectResponse> resp = projectService.editProject(req);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    public void testEditProjectForbidden() {
        mockUser("organizer", 1L);
        EditProjectRequest req = new EditProjectRequest();
        req.setProjectId(100L);

        User organizer = new User(); organizer.setId(2L); // Different user
        Project project = new Project();
        project.setProjectId(100L);
        project.setOrganizer(organizer);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        ResponseEntity<ProjectResponse> resp = projectService.editProject(req);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    public void testEditProjectValidationFailures() {
        mockUser("organizer", 1L);
        User organizer = new User(); organizer.setId(1L);
        Project project = new Project();
        project.setProjectId(100L);
        project.setOrganizer(organizer);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        // Min > Max
        EditProjectRequest req = new EditProjectRequest();
        req.setProjectId(100L);
        req.setMinNrParticipants(10);
        req.setMaxNrParticipants(5);
        ResponseEntity<ProjectResponse> resp = projectService.editProject(req);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        // Invalid questions
        req = new EditProjectRequest();
        req.setProjectId(100L);
        FormQuestionDTO q1 = new FormQuestionDTO(); q1.setQuestionNumber(2);
        req.setFormQuestions(List.of(q1));
        resp = projectService.editProject(req);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testEditProjectWithAllOptions() {
        mockUser("organizer", 1L);
        EditProjectRequest req = new EditProjectRequest();
        req.setProjectId(100L);
        req.setProjectName("Updated");
        req.setRoleOptions(List.of("Role1"));
        req.setBackgroundOptions(List.of("Bkg1"));
        req.setRolesQuestionText("Roles?");
        req.setBackgroundQuestionText("Background?");
        req.setFormQuestions(Collections.emptyList());
        req.setSchedules(Collections.emptyList());

        User organizer = new User(); organizer.setId(1L);
        Project project = new Project();
        project.setProjectId(100L);
        project.setOrganizer(organizer);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(project);
        when(scheduleRepository.findByProjectProjectId(100L)).thenReturn(Collections.emptyList());

        ResponseEntity<ProjectResponse> resp = projectService.editProject(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(questionRepository, times(2)).deleteQuestions(100);
    }

    @Test
    public void testDeleteProjectSuccess() {
        Long projectId = 100L;
        mockUser("organizer", 1L);
        when(projectRepository.existsByProjectIdAndOrganizer(eq(projectId), any(User.class))).thenReturn(true);

        ProjectResponse resp = projectService.deleteProject(projectId);
        assertEquals("Success", resp.getStatus());
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    public void testDeleteProjectNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        ProjectResponse resp = projectService.deleteProject(100L);
        assertEquals("Failure", resp.getStatus());
        assertEquals("User not authenticated", resp.getMessage());
    }

    @Test
    public void testDeleteProjectNotFoundOrForbidden() {
        mockUser("organizer", 1L);
        when(projectRepository.existsByProjectIdAndOrganizer(eq(100L), any(User.class))).thenReturn(false);

        ProjectResponse resp = projectService.deleteProject(100L);
        assertEquals("Failure", resp.getStatus());
        assertTrue(resp.getMessage().contains("not found") || resp.getMessage().contains("permission"));
    }

    @Test
    public void testGetAllUserProjects() {
        mockUser("organizer", 1L);
        User organizer = new User(); organizer.setId(1L);

        GetProjectResponse p1 = new GetProjectResponse();
        p1.setProjectId(1L);
        p1.setProjectName("Project 1");

        when(projectRepository.findMainProjInfoByOrganizer(any(User.class))).thenReturn(List.of(p1));
        when(questionRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        List<GetProjectResponse> projects = projectService.getAllUserProjects();
        assertEquals(1, projects.size());
        assertEquals("Project 1", projects.get(0).getProjectName());
    }

    @Test
    public void testGetAllUserProjectsNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertThrows(RuntimeException.class, () -> projectService.getAllUserProjects());
    }

    @Test
    public void testCanUserViewSchedule_Everybody() {
        Project project = new Project();
        project.setScheduleVisibility(ScheduleVisibility.EVERYBODY);

        assertTrue(projectService.canUserViewSchedule(project, null));
    }

    @Test
    public void testCanUserViewSchedule_Organizer() {
        User organizer = new User(); organizer.setId(1L);
        User user = new User(); user.setId(1L);

        Project project = new Project();
        project.setScheduleVisibility(ScheduleVisibility.APPLICANTS);
        project.setOrganizer(organizer);

        assertTrue(projectService.canUserViewSchedule(project, user));
    }

    @Test
    public void testCanUserViewSchedule_Applicants() {
        User organizer = new User(); organizer.setId(1L);
        User user = new User(); user.setId(2L);

        Project project = new Project();
        project.setScheduleVisibility(ScheduleVisibility.APPLICANTS);
        project.setOrganizer(organizer);

        Applicant applicant = new Applicant();
        when(applicantRepository.getIsSelectedByUserAndProject(user, project)).thenReturn(Optional.of(applicant.getIsSelected()));

        assertTrue(projectService.canUserViewSchedule(project, user));
    }

    @Test
    public void testCanUserViewSchedule_AcceptedParticipants() {
        User organizer = new User(); organizer.setId(1L);
        User user = new User(); user.setId(2L);

        Project project = new Project();
        project.setScheduleVisibility(ScheduleVisibility.ACCEPTED_PARTICIPANTS);
        project.setOrganizer(organizer);

        Applicant applicant = new Applicant();
        applicant.setIsSelected(true);
        when(applicantRepository.getIsSelectedByUserAndProject(user, project)).thenReturn(Optional.of(applicant.getIsSelected()));

        assertTrue(projectService.canUserViewSchedule(project, user));
    }

    @Test
    public void testCanUserViewSchedule_NotApplicant() {
        User organizer = new User(); organizer.setId(1L);
        User user = new User(); user.setId(2L);

        Project project = new Project();
        project.setScheduleVisibility(ScheduleVisibility.APPLICANTS);
        project.setOrganizer(organizer);

        when(applicantRepository.getIsSelectedByUserAndProject(user, project))
                .thenReturn(Optional.empty());

        assertFalse(projectService.canUserViewSchedule(project, user));
    }

    @Test
    public void testCanUserViewSchedule_NotAccepted() {
        User organizer = new User(); organizer.setId(1L);
        User user = new User(); user.setId(2L);

        Project project = new Project();
        project.setScheduleVisibility(ScheduleVisibility.ACCEPTED_PARTICIPANTS);
        project.setOrganizer(organizer);

        Applicant applicant = new Applicant();
        applicant.setIsSelected(false);
        when(applicantRepository.getIsSelectedByUserAndProject(user, project))
                .thenReturn(Optional.of(false));

        assertFalse(projectService.canUserViewSchedule(project, user));
    }

    @Test
    public void testGetProjectById() {
        Project project = new Project();
        project.setProjectId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(1L);
        assertEquals(1L, result.getProjectId());
    }

    @Test
    public void testGetProjectByIdNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> projectService.getProjectById(1L));
    }

    @Test
    public void testGetScheduleByDay() {
        ProjectSchedule schedule = new ProjectSchedule();
        schedule.setActivityTitle("Activity");
        schedule.setActivityDescription("Description");

        when(scheduleRepository.findByProjectProjectIdAndDayNumber(1L, 1)).thenReturn(List.of(schedule));

        List<ScheduleDTO> schedules = projectService.getScheduleByDay(1L, 1);
        assertEquals(1, schedules.size());
        assertEquals("Activity", schedules.get(0).getActivityTitle());
    }
}
