package com.example.timesaver.controller;

import com.example.timesaver.model.Assignment;
import com.example.timesaver.model.Submission;
import com.example.timesaver.service.AssignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController assignmentController;

    @Test
    public void testCreateAssignment() {
        Assignment assignment = new Assignment();
        when(assignmentService.createAssignment(any())).thenReturn(assignment);

        ResponseEntity<Assignment> response = assignmentController.createAssignment(assignment);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(assignment, response.getBody());
    }

    @Test
    public void testGetAssignmentsByProject() {
        List<Assignment> assignments = Collections.emptyList();
        when(assignmentService.getAssignmentsByProject(1L)).thenReturn(assignments);

        ResponseEntity<List<Assignment>> response = assignmentController.getAssignmentsByProject(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(assignments, response.getBody());
    }

    @Test
    public void testSubmitAssignment() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        Submission submission = new Submission();
        
        when(assignmentService.submitAssignment(any(), any(), any(), any())).thenReturn(submission);

        ResponseEntity<Submission> response = assignmentController.submitAssignment(1L, "text", null, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetTeamSubmission() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        Submission submission = new Submission();
        
        when(assignmentService.getTeamSubmission(any(), any())).thenReturn(submission);

        ResponseEntity<Submission> response = assignmentController.getTeamSubmission(1L, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetAllSubmissions() {
        List<Submission> submissions = Collections.emptyList();
        when(assignmentService.getAllSubmissionsForAssignment(1L)).thenReturn(submissions);

        ResponseEntity<List<Submission>> response = assignmentController.getAllSubmissions(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(submissions, response.getBody());
    }

    @Test
    public void testSubmitAssignmentWithException() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");

        when(assignmentService.submitAssignment(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Submission error"));

        try {
            assignmentController.submitAssignment(1L, "text", null, auth);
        } catch (RuntimeException e) {
            assertEquals("Submission error", e.getMessage());
        }
    }

    @Test
    public void testGetTeamSubmissionWithException() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");

        when(assignmentService.getTeamSubmission(any(), any()))
            .thenThrow(new RuntimeException("Not found"));

        try {
            assignmentController.getTeamSubmission(1L, auth);
        } catch (RuntimeException e) {
            assertEquals("Not found", e.getMessage());
        }
    }
}
