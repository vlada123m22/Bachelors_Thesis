package com.example.timesaver.controller;

import com.example.timesaver.model.*;
import com.example.timesaver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        return ResponseEntity.ok(assignmentService.createAssignment(assignment));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Assignment>> getAssignmentsByProject(@PathVariable Integer projectId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByProject(projectId));
    }

    @PostMapping("/{assignmentId}/submission")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<Submission> submitAssignment(
            @PathVariable Integer assignmentId,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) MultipartFile file,
            Authentication authentication) throws IOException {

        String username = authentication.getName();
        return ResponseEntity.ok(assignmentService.submitAssignment(assignmentId, username, text, file));
    }

    @GetMapping("/{assignmentId}/submission")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'MENTOR', 'ORGANIZER')")
    public ResponseEntity<Submission> getTeamSubmission(
            @PathVariable Integer assignmentId,
            Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(assignmentService.getTeamSubmission(assignmentId, username));
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('MENTOR', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<Submission>> getAllSubmissions(@PathVariable Integer assignmentId) {
        return ResponseEntity.ok(assignmentService.getAllSubmissionsForAssignment(assignmentId));
    }
}