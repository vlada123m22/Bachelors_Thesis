package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.AssignmentRepository;
import com.example.timesaver.repository.SubmissionRepository;
import com.example.timesaver.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
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

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private UserRepository userRepository;
    @Mock private ApplicantRepository applicantRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    public void testSubmitAssignmentSuccess() throws IOException {
        String username = "user";
        Integer assignmentId = 1;
        User user = new User();
        user.setId(10);
        Assignment assignment = new Assignment();
        Project project = new Project();
        project.setProjectId(100);
        assignment.setProject(project);

        Applicant applicant = new Applicant();
        Team team = new Team();
        team.setTeamId(5);
        team.setProject(project);
        applicant.setTeam(team);

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        when(submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, 5)).thenReturn(Optional.empty());
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
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));

        assertThrows(RuntimeException.class, () ->
                assignmentService.submitAssignment(1, username, "text", null));
    }

    @Test
    public void testCreateAssignment() {
        Assignment a = new Assignment();
        when(assignmentRepository.save(a)).thenReturn(a);
        assertEquals(a, assignmentService.createAssignment(a));
    }

    @Test
    public void testGetAssignmentsByProject() {
        when(assignmentRepository.findByProjectProjectId(1)).thenReturn(Collections.emptyList());
        assertTrue(assignmentService.getAssignmentsByProject(1).isEmpty());
    }

    @Test
    public void testGetTeamSubmissionSuccess() {
        String username = "user";
        Integer assignmentId = 1;
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        Applicant applicant = new Applicant();
        Team team = new Team();
        team.setTeamId(5);
        applicant.setTeam(team);

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        Submission sub = new Submission();
        when(submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, 5)).thenReturn(Optional.of(sub));

        assertEquals(sub, assignmentService.getTeamSubmission(assignmentId, username));
    }

    @Test
    public void testGetAllSubmissionsForAssignment() {
        when(assignmentRepository.existsById(1)).thenReturn(true);
        when(submissionRepository.findByAssignmentId(1)).thenReturn(List.of(new Submission()));
        assertEquals(1, assignmentService.getAllSubmissionsForAssignment(1).size());
    }

    @Test
    public void testGetAllSubmissionsThrowsWhenNotFound() {
        when(assignmentRepository.existsById(1)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> assignmentService.getAllSubmissionsForAssignment(1));
    }

    @Test
    @DisplayName("Should throw when user not found in submitAssignment")
    void testSubmitAssignmentUserNotFound() {
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.submitAssignment(1, "user", "text", null));
    }

    @Test
    @DisplayName("Should throw when assignment not found in submitAssignment")
    void testSubmitAssignmentNotFound() {
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(new User()));
        when(assignmentRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.submitAssignment(1, "user", "text", null));
    }

    @Test
    @DisplayName("Should throw when user is not participant in submitAssignment")
    void testSubmitAssignmentNotParticipant() {
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.submitAssignment(1, "user", "text", null));
    }

    @Test
    @DisplayName("Should submit assignment via direct team and user call")
    void testSubmitAssignmentDirect() throws IOException {
        Team team = new Team();
        team.setTeamId(5);
        Project project = new Project();
        project.setProjectId(100);
        team.setProject(project);
        User user = new User();
        user.setId(10);

        when(submissionRepository.findByAssignmentIdAndTeamTeamId(1, 5)).thenReturn(Optional.empty());
        when(assignmentRepository.getReferenceById(1)).thenReturn(new Assignment());
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Submission result = assignmentService.submitAssignment(1, team, user, "text", null);
        assertEquals(team, result.getTeam());
        assertEquals(user, result.getUploadedBy());
    }

    @Test
    @DisplayName("Should throw when user not found in getTeamSubmission")
    void testGetTeamSubmissionUserNotFound() {
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.getTeamSubmission(1, "user"));
    }

    @Test
    @DisplayName("Should throw when assignment not found in getTeamSubmission")
    void testGetTeamSubmissionAssignmentNotFound() {
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(new User()));
        when(assignmentRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.getTeamSubmission(1, "user"));
    }

    @Test
    @DisplayName("Should throw when user not part of team in getTeamSubmission")
    void testGetTeamSubmissionNoTeam() {
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        Applicant applicant = new Applicant(); // team is null
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        assertThrows(RuntimeException.class, () -> assignmentService.getTeamSubmission(1, "user"));
    }

    @Test
    @DisplayName("Should throw when no submission found in getTeamSubmission")
    void testGetTeamSubmissionNoSub() {
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        Applicant applicant = new Applicant();
        Team team = new Team();
        team.setTeamId(5);
        applicant.setTeam(team);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        when(submissionRepository.findByAssignmentIdAndTeamTeamId(1, 5)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.getTeamSubmission(1, "user"));
    }

    @Test
    @DisplayName("Should submit assignment without file")
    void testSubmitAssignmentWithoutFile() throws IOException {
        String username = "user";
        Integer assignmentId = 1;
        User user = new User();
        user.setId(10);
        Assignment assignment = new Assignment();
        Project project = new Project();
        project.setProjectId(100);
        assignment.setProject(project);

        Applicant applicant = new Applicant();
        Team team = new Team();
        team.setTeamId(5);
        team.setProject(project);
        applicant.setTeam(team);

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.of(applicant));
        when(submissionRepository.findByAssignmentIdAndTeamTeamId(assignmentId, 5)).thenReturn(Optional.empty());
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Submission result = assignmentService.submitAssignment(assignmentId, username, "text only", null);
        assertNotNull(result);
        assertEquals("text only", result.getTextContent());
        assertNull(result.getFilePath());
    }

    @Test
    @DisplayName("Should submit assignment direct with file")
    void testSubmitAssignmentDirectWithFile() throws IOException {
        Team team = new Team();
        team.setTeamId(5);
        Project project = new Project();
        project.setProjectId(100);
        team.setProject(project);
        User user = new User();
        user.setId(10);

        when(submissionRepository.findByAssignmentIdAndTeamTeamId(1, 5)).thenReturn(Optional.empty());
        when(assignmentRepository.getReferenceById(1)).thenReturn(new Assignment());
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(fileStorageService.storeFile(any(), any(), any(), any())).thenReturn("path/to/file");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        Submission result = assignmentService.submitAssignment(1, team, user, "text", file);
        assertEquals("path/to/file", result.getFilePath());
    }

    @Test
    @DisplayName("Should throw when user not participant in getTeamSubmission")
    void testGetTeamSubmissionNotParticipant() {
        User user = new User();
        Assignment assignment = new Assignment();
        Project project = new Project();
        assignment.setProject(project);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(applicantRepository.findByUserAndProject(user, project)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> assignmentService.getTeamSubmission(1, "user"));
    }
}