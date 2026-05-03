package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicantSelectionServiceTest {

    @Mock private ApplicantRepository applicantRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ApplicantSelectionService applicantSelectionService;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUser(String username, Integer userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
    }

    @Test
    public void testSetApplicantSelectionSuccess() {
        Integer projectId = 1;
        Integer applicantId = 2;
        Integer userId = 3;

        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setProject(project);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));

        applicantSelectionService.setApplicantSelection(projectId, applicantId, true);

        assertTrue(applicant.getIsSelected());
        verify(applicantRepository).save(applicant);
    }

    @Test
    public void testSetApplicantSelectionThrowsWhenNotOrganizer() {
        Integer projectId = 1;
        Integer userId = 3;
        mockUser("other", 4);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(SecurityException.class, () ->
                applicantSelectionService.setApplicantSelection(projectId, 2, true));
    }

    @Test
    public void testBulkSetSelectionSuccess() {
        Integer projectId = 1;
        Integer userId = 3;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        Applicant a1 = new Applicant();
        a1.setProject(project);
        Applicant a2 = new Applicant();
        a2.setProject(project);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findAllById(any())).thenReturn(List.of(a1, a2));

        int updated = applicantSelectionService.bulkSetSelection(projectId, List.of(10, 11), true);

        assertEquals(2, updated);
        assertTrue(a1.getIsSelected());
        assertTrue(a2.getIsSelected());
        verify(applicantRepository).saveAll(any());
    }

    @Test
    public void testBulkSetSelectionThrowsWhenWrongProject() {
        Integer projectId = 1;
        mockUser("organizer", 3);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(3);
        project.setOrganizer(organizer);

        Project otherProject = new Project();
        otherProject.setProjectId(99);

        Applicant a1 = new Applicant();
        a1.setProject(otherProject);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findAllById(any())).thenReturn(List.of(a1));

        assertThrows(IllegalArgumentException.class, () ->
                applicantSelectionService.bulkSetSelection(projectId, List.of(10), true));
    }

    @Test
    public void testGetCurrentUserReturnsNullWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        Integer projectId = 1;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        assertThrows(SecurityException.class, () ->
                applicantSelectionService.setApplicantSelection(projectId, 2, true));
    }

    @Test
    public void testGetCurrentUserReturnsNullWhenAuthenticationNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        Integer projectId = 1;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        assertThrows(SecurityException.class, () ->
                applicantSelectionService.setApplicantSelection(projectId, 2, true));
    }

    @Test
    public void testProjectNotFound() {
        when(projectRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                applicantSelectionService.setApplicantSelection(1, 2, true));
    }

    @Test
    public void testApplicantNotFound() {
        Integer projectId = 1;
        Integer userId = 3;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                applicantSelectionService.setApplicantSelection(projectId, 2, true));
    }

    @Test
    public void testApplicantBelongsToWrongProject() {
        Integer projectId = 1;
        Integer applicantId = 2;
        Integer userId = 3;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        Project otherProject = new Project();
        otherProject.setProjectId(99);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setProject(otherProject);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));

        assertThrows(IllegalArgumentException.class, () ->
                applicantSelectionService.setApplicantSelection(projectId, applicantId, true));
    }

    @Test
    public void testSetApplicantSelectionToFalse() {
        Integer projectId = 1;
        Integer applicantId = 2;
        Integer userId = 3;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setProject(project);
        applicant.setIsSelected(true);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));

        applicantSelectionService.setApplicantSelection(projectId, applicantId, false);

        assertFalse(applicant.getIsSelected());
        verify(applicantRepository).save(applicant);
    }

    @Test
    public void testBulkSetSelectionToFalse() {
        Integer projectId = 1;
        Integer userId = 3;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        Applicant a1 = new Applicant();
        a1.setProject(project);
        a1.setIsSelected(true);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findAllById(any())).thenReturn(List.of(a1));

        int updated = applicantSelectionService.bulkSetSelection(projectId, List.of(10), false);

        assertEquals(1, updated);
        assertFalse(a1.getIsSelected());
        verify(applicantRepository).saveAll(any());
    }

    @Test
    public void testGetCurrentUserNotFoundInRepository() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("ghost");
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        Project project = new Project();
        project.setProjectId(1);
        User organizer = new User(); organizer.setId(99);
        project.setOrganizer(organizer);
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        assertThrows(SecurityException.class, () ->
                applicantSelectionService.setApplicantSelection(1, 2, true));
    }
}