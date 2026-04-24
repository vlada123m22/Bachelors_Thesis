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

    @Mock
    private ApplicantRepository applicantRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ApplicantSelectionService applicantSelectionService;

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
    public void testSetApplicantSelectionSuccess() {
        Long projectId = 1L;
        Long applicantId = 2L;
        Long userId = 3L;
        String username = "organizer";

        mockUser(username, userId);

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
        Long projectId = 1L;
        Long userId = 3L;
        mockUser("other", 4L);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(SecurityException.class, () -> {
            applicantSelectionService.setApplicantSelection(projectId, 2L, true);
        });
    }

    @Test
    public void testBulkSetSelectionSuccess() {
        Long projectId = 1L;
        Long userId = 3L;
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

        int updated = applicantSelectionService.bulkSetSelection(projectId, List.of(10L, 11L), true);

        assertEquals(2, updated);
        assertTrue(a1.getIsSelected());
        assertTrue(a2.getIsSelected());
        verify(applicantRepository).saveAll(any());
    }

    @Test
    public void testBulkSetSelectionThrowsWhenWrongProject() {
        Long projectId = 1L;
        mockUser("organizer", 3L);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(3L);
        project.setOrganizer(organizer);

        Project otherProject = new Project();
        otherProject.setProjectId(99L);

        Applicant a1 = new Applicant();
        a1.setProject(otherProject);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findAllById(any())).thenReturn(List.of(a1));

        assertThrows(IllegalArgumentException.class, () -> {
            applicantSelectionService.bulkSetSelection(projectId, List.of(10L), true);
        });
    }

    @Test
    public void testGetCurrentUserReturnsNullWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        Long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        assertThrows(SecurityException.class, () -> {
            applicantSelectionService.setApplicantSelection(projectId, 2L, true);
        });
    }

    @Test
    public void testGetCurrentUserReturnsNullWhenAuthenticationNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        Long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        assertThrows(SecurityException.class, () -> {
            applicantSelectionService.setApplicantSelection(projectId, 2L, true);
        });
    }

    @Test
    public void testProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            applicantSelectionService.setApplicantSelection(1L, 2L, true);
        });
    }

    @Test
    public void testApplicantNotFound() {
        Long projectId = 1L;
        Long userId = 3L;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            applicantSelectionService.setApplicantSelection(projectId, 2L, true);
        });
    }

    @Test
    public void testApplicantBelongsToWrongProject() {
        Long projectId = 1L;
        Long applicantId = 2L;
        Long userId = 3L;
        mockUser("organizer", userId);

        Project project = new Project();
        project.setProjectId(projectId);
        User organizer = new User();
        organizer.setId(userId);
        project.setOrganizer(organizer);

        Project otherProject = new Project();
        otherProject.setProjectId(99L);

        Applicant applicant = new Applicant();
        applicant.setApplicantId(applicantId);
        applicant.setProject(otherProject);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));

        assertThrows(IllegalArgumentException.class, () -> {
            applicantSelectionService.setApplicantSelection(projectId, applicantId, true);
        });
    }

    @Test
    public void testSetApplicantSelectionToFalse() {
        Long projectId = 1L;
        Long applicantId = 2L;
        Long userId = 3L;
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
        Long projectId = 1L;
        Long userId = 3L;
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

        int updated = applicantSelectionService.bulkSetSelection(projectId, List.of(10L), false);

        assertEquals(1, updated);
        assertFalse(a1.getIsSelected());
        verify(applicantRepository).saveAll(any());
    }
}
