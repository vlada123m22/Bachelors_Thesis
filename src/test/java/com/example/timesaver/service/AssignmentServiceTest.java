package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.AssignmentRepository;
import com.example.timesaver.repository.SubmissionRepository;
import com.example.timesaver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicantRepository applicantRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    public void testSubmitAssignmentSuccess() throws IOException {
        String username = "user";
        Long assignmentId = 1L;
        User user = new User();
        user.setId(10L);
        Assignment assignment = new Assignment();
        Project project = new Project();
        project.setProjectId(100L);
        assignment.setProject(project);
        
        Applicant applicant = new Applicant();
        Team team = new Team();
        team.setTeamId(5L);
        team.setProject(project);
        applicant.setTeam(team);

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        when(submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, 5L)).thenReturn(Optional.empty());
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(fileStorageService.storeFile(any(), any(), any(), any())).thenReturn("path/to/file");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        Submission result = assignmentService.submitAssignment(assignmentId, username, "some text", file);

        assertNotNull(result);
        assertEquals("some text", result.getTextContent());
        assertEquals("path/to/file", result.getFilePath());
        assertEquals(user, result.getUploadedBy());
    }

    @Test
    public void testSubmitAssignmentThrowsWhenNoTeam() {
        String username = "user";
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        Applicant applicant = new Applicant(); // no team

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));

        assertThrows(RuntimeException.class, () -> {
            assignmentService.submitAssignment(1L, username, "text", null);
        });
    }

    @Test
    public void testCreateAssignment() {
        Assignment a = new Assignment();
        when(assignmentRepository.save(a)).thenReturn(a);
        assertEquals(a, assignmentService.createAssignment(a));
    }

    @Test
    public void testGetAssignmentsByProject() {
        when(assignmentRepository.findByProjectProjectId(1L)).thenReturn(Collections.emptyList());
        assertTrue(assignmentService.getAssignmentsByProject(1L).isEmpty());
    }

    @Test
    public void testGetTeamSubmissionSuccess() {
        String username = "user";
        Long assignmentId = 1L;
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        Applicant applicant = new Applicant();
        Team team = new Team();
        team.setTeamId(5L);
        applicant.setTeam(team);

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        Submission sub = new Submission();
        when(submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, 5L)).thenReturn(Optional.of(sub));

        assertEquals(sub, assignmentService.getTeamSubmission(assignmentId, username));
    }

    @Test
    public void testGetAllSubmissionsForAssignment() {
        when(assignmentRepository.existsById(1L)).thenReturn(true);
        when(submissionRepository.findByAssignmentId(1L)).thenReturn(List.of(new Submission()));
        assertEquals(1, assignmentService.getAllSubmissionsForAssignment(1L).size());
    }

    @Test
    public void testGetAllSubmissionsThrowsWhenNotFound() {
        when(assignmentRepository.existsById(1L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> assignmentService.getAllSubmissionsForAssignment(1L));
    }
}
