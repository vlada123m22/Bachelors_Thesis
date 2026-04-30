package com.example.timesaver.controller;

import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.project.*;
import com.example.timesaver.service.ProjectService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * Create a new project
     * POST /projects
     * Only ORGANIZER and ADMIN roles can create projects
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);

        if ("Success".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get project by ID
     * GET /projects/{projectId}
     * Returns project details with all form questions
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable Integer projectId, @RequestHeader("X-Timezone") String userTimezone) {
        try {
            GetProjectResponse response = projectService.getProject(projectId, userTimezone);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ProjectResponse("Failure", e.getMessage()));
        }
    }

    /**
     * Update existing project
     * PUT /projects
     * Only the project organizer can edit their project
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> editProject(@Valid @RequestBody EditProjectRequest request) {
            return projectService.editProject(request);
    }

    /**
     * Delete project by ID
     * DELETE /projects/{projectId}
     * Only the project organizer can delete their project
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> deleteProject(@PathVariable Integer projectId) {
        ProjectResponse response = projectService.deleteProject(projectId);
        if ("Success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else if (response.getMessage().equals("User not authenticated")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (response.getMessage().equals("Project not found or you don't have permission to delete it")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all projects for the current user(organizer)
     * GET /projects
     * Returns all projects created by the authenticated organizer
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getAllUserProjects() {
        try {
            List<GetProjectResponse> projects = projectService.getAllUserProjects();
            return ResponseEntity.ok(projects);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ProjectResponse("Failure", e.getMessage()));
        }
    }

    @GetMapping("/{projectId}/schedule/{dayNumber}")
    public ResponseEntity<?> getScheduleForDay(@PathVariable Integer projectId, @PathVariable Integer dayNumber) {
        Project project = projectService.getProjectById(projectId);
        User user = projectService.getCurrentUser();

        if (!projectService.canUserViewSchedule(project, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to schedule");
        }

        List<ScheduleDTO> schedule = projectService.getScheduleByDay(projectId, dayNumber);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Get dashboard with future projects for participants
     * GET /projects/dashboard/future
     * Returns all projects that will take place in the future, ordered by start date
     */
    @GetMapping("/dashboard/future")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ADMIN')")
    public ResponseEntity<List<ProjectDashboardDTO>> getFutureProjects() {
        List<com.example.timesaver.model.dto.project.ProjectDashboardDTO> projects = projectService.getFutureProjects();
        return ResponseEntity.ok(projects);
    }
}