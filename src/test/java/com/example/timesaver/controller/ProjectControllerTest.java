package com.example.timesaver.controller;

import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.project.*;
import com.example.timesaver.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Test
    public void testCreateProjectSuccess() {
        CreateProjectRequest req = new CreateProjectRequest();
        ProjectResponse resp = new ProjectResponse("Success", null, 1L);
        when(projectService.createProject(any())).thenReturn(resp);

        ResponseEntity<ProjectResponse> response = projectController.createProject(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testCreateProjectFailure() {
        CreateProjectRequest req = new CreateProjectRequest();
        ProjectResponse resp = new ProjectResponse("Failure", "Error");
        when(projectService.createProject(any())).thenReturn(resp);

        ResponseEntity<ProjectResponse> response = projectController.createProject(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetProjectSuccess() {
        GetProjectResponse resp = new GetProjectResponse(1L, "p", "d", null, null, 10, 1, Collections.emptyList());
        when(projectService.getProject(1L, "UTC")).thenReturn(resp);

        ResponseEntity<?> response = projectController.getProject(1L, "UTC");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetProjectNotFound() {
        when(projectService.getProject(1L, "UTC")).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = projectController.getProject(1L, "UTC");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testEditProject() {
        EditProjectRequest req = new EditProjectRequest();
        ResponseEntity<ProjectResponse> resp = ResponseEntity.ok(new ProjectResponse("Success", null));
        when(projectService.editProject(req)).thenReturn(resp);

        ResponseEntity<ProjectResponse> response = projectController.editProject(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeleteProjectSuccess() {
        ProjectResponse resp = new ProjectResponse("Success", null);
        when(projectService.deleteProject(1L)).thenReturn(resp);

        ResponseEntity<ProjectResponse> response = projectController.deleteProject(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeleteProjectFailure() {
        ProjectResponse resp = new ProjectResponse("Failure", "Error");
        when(projectService.deleteProject(1L)).thenReturn(resp);

        ResponseEntity<ProjectResponse> response = projectController.deleteProject(1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetAllUserProjectsSuccess() {
        List<GetProjectResponse> projects = Collections.emptyList();
        when(projectService.getAllUserProjects()).thenReturn(projects);

        ResponseEntity<?> response = projectController.getAllUserProjects();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetAllUserProjectsFailure() {
        when(projectService.getAllUserProjects()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = projectController.getAllUserProjects();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testGetScheduleForDaySuccess() {
        Project project = new Project();
        User user = new User();
        when(projectService.getProjectById(1L)).thenReturn(project);
        when(projectService.getCurrentUser()).thenReturn(user);
        when(projectService.canUserViewSchedule(project, user)).thenReturn(true);
        when(projectService.getScheduleByDay(1L, 1)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = projectController.getScheduleForDay(1L, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetScheduleForDayForbidden() {
        Project project = new Project();
        User user = new User();
        when(projectService.getProjectById(1L)).thenReturn(project);
        when(projectService.getCurrentUser()).thenReturn(user);
        when(projectService.canUserViewSchedule(project, user)).thenReturn(false);

        ResponseEntity<?> response = projectController.getScheduleForDay(1L, 1);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
