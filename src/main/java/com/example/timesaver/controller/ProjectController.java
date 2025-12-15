package com.example.timesaver.controller;

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
    public ResponseEntity<?> getProject(@PathVariable Long projectId, @RequestHeader("X-Timezone") String userTimezone) {
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
        ProjectResponse response = projectService.editProject(request);

        if ("Success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete project by ID
     * DELETE /projects/{projectId}
     * Only the project organizer can delete their project
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> deleteProject(@PathVariable Long projectId) {
        ProjectResponse response = projectService.deleteProject(projectId);

        if ("Success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
}