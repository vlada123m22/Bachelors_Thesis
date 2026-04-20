package com.example.timesaver.service;

import com.example.timesaver.exceptions.ApplicationException;
import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.application.*;
import com.example.timesaver.model.dto.project.FormQuestionDTO;
import com.example.timesaver.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private QuestionAnswerRepository questionAnswerRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private QuestionRepository questionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.setField(applicationService, "userRepository", userRepository);
    }

    private void mockUser(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("notAnonymous");
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(new User()));
    }

    @Test
    public void testGetFormForProject() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestion("How are you?");
        q.setQuestionType(QuestionType.TEXT);
        
        when(questionRepository.findByProjectId(projectId)).thenReturn(List.of(q));

        GetFormResponse result = applicationService.getFormForProject(projectId);
        assertEquals(1, result.getFormQuestions().size());
        assertEquals("How are you?", result.getFormQuestions().get(0).getQuestion());
    }

    @Test
    public void testGetFormForProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> applicationService.getFormForProject(1L));
    }

    @Test
    public void testSubmitApplicationSingleNoTeamSuccess() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setTeamsPreformed(false);
        project.setRolesOptions("Dev|Design");
        project.setBackgroundOptions("CS|Art");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("testuser");

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setJoinExistentTeam(false);
        req.setQuestionsAnswers(Collections.emptyList());

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());

        assertEquals("Success", resp.getStatus());
        verify(applicantRepository).save(any(Applicant.class));
    }

    @Test
    public void testSubmitApplicationWithTeamNameAndTeammatesSuccess() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setTeamsPreformed(false);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("testuser");

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setTeamName("Cool Team");
        req.setJoinExistentTeam(false);
        TeammateDTO teammate = new TeammateDTO();
        teammate.setFirstName("Jane");
        teammate.setLastName("Doe");
        req.setTeammates(List.of(teammate));
        req.setQuestionsAnswers(Collections.emptyList());

        when(teamRepository.findByTeamNameIgnoreCaseAndProject(anyString(), eq(project))).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(i -> i.getArguments()[0]);

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertEquals("Success", resp.getStatus());
        verify(teamRepository).save(any(Team.class));
        verify(applicantRepository, atLeast(2)).save(any(Applicant.class));
    }

    @Test
    public void testSubmitApplicationJoinExistingTeamSuccess() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setTeamsPreformed(false);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("testuser");

        Team existingTeam = new Team();
        existingTeam.setTeamName("Cool Team");
        when(teamRepository.findByTeamNameIgnoreCaseAndProject("Cool Team", project)).thenReturn(Optional.of(existingTeam));

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTeamName("Cool Team");
        req.setJoinExistentTeam(true);
        TeammateDTO teammate = new TeammateDTO();
        teammate.setFirstName("Jane");
        teammate.setLastName("Doe");
        req.setTeammates(List.of(teammate));
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setQuestionsAnswers(Collections.emptyList());

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertEquals("Success", resp.getStatus());
    }

    @Test
    public void testSubmitApplicationTeamAlreadyExistsFailure() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setTeamsPreformed(false);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("testuser");

        Team existingTeam = new Team();
        when(teamRepository.findByTeamNameIgnoreCaseAndProject("Cool Team", project)).thenReturn(Optional.of(existingTeam));

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setTeamName("Cool Team");
        req.setJoinExistentTeam(false);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertEquals("Failure", resp.getStatus());
        assertTrue(resp.getTeamExists());
    }

    @Test
    public void testSubmitApplicationInvalidRole() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setRoles(List.of("Manager"));
        req.setTimezone("UTC");

        assertThrows(ApplicationException.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationMissingTeamNameWithTeammates() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("test");

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setTeammates(List.of(new TeammateDTO()));
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));

        assertThrows(ApplicationException.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationTeamWithOnlyOneMember() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("test");

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setTeamName("My Team");
        req.setTeammates(Collections.emptyList());
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));

        assertThrows(ApplicationException.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationQuestionsHandling() throws IOException {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("test");

        FormQuestion qText = new FormQuestion();
        qText.setQuestionNumber(1);
        qText.setQuestionType(QuestionType.TEXT);
        qText.setQuestion("Name?");

        FormQuestion qFile = new FormQuestion();
        qFile.setQuestionNumber(2);
        qFile.setQuestionType(QuestionType.FILE);
        qFile.setQuestion("CV?");

        when(questionRepository.findByProjectId(projectId)).thenReturn(List.of(qText, qFile));

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("John");
        req.setLastName("Doe");

        QuestionAnswerDTO a1 = new QuestionAnswerDTO();
        a1.setQuestionNumber(1);
        a1.setQuestionType(QuestionType.TEXT);
        a1.setQuestion("Name?");
        a1.setAnswer("John");

        QuestionAnswerDTO a2 = new QuestionAnswerDTO();
        a2.setQuestionNumber(2);
        a2.setQuestionType(QuestionType.FILE);
        a2.setQuestion("CV?");

        req.setQuestionsAnswers(List.of(a1, a2));

        when(applicantRepository.save(any(Applicant.class))).thenAnswer(i -> {
            Applicant a = i.getArgument(0);
            if (a.getApplicantId() == null) a.setApplicantId(100L);
            return a;
        });

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(any(), any(), any(), any())).thenReturn("path/to/file");

        Map<Integer, MultipartFile> files = new HashMap<>();
        files.put(2, mockFile);

        ApplicationResponse resp = applicationService.submitApplication(req, files);
        assertEquals("Success", resp.getStatus());
        verify(questionAnswerRepository, times(2)).save(any(QuestionAnswer.class));
    }

    @Test
    public void testSubmitApplicationThrowsOnInvalidTimezone() {
        Project project = new Project();
        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setTimezone("Invalid/Zone");

        assertThrows(ApplicationException.class, () -> {
            applicationService.submitApplication(req, Collections.emptyMap());
        });
    }

    @Test
    public void testSubmitApplicationThrowsWhenAccountRequiredButMissing() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setTeamsPreformed(false);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(securityContext.getAuthentication()).thenReturn(null);

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);

        assertThrows(ApplicationException.class, () -> {
            applicationService.submitApplication(req, Collections.emptyMap());
        });
    }

    @Test
    public void testSubmitApplicationQuestionMismatch() {
        Long projectId = 1L;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setRolesOptions("Dev");
        project.setBackgroundOptions("CS");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockUser("test");

        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestionType(QuestionType.TEXT);
        q.setQuestion("Actual Question");
        when(questionRepository.findByProjectId(projectId)).thenReturn(List.of(q));

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(projectId);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("A"); req.setLastName("B");

        QuestionAnswerDTO a = new QuestionAnswerDTO();
        a.setQuestionNumber(1);
        a.setQuestionType(QuestionType.TEXT);
        a.setQuestion("Wrong Question");
        req.setQuestionsAnswers(List.of(a));

        when(applicantRepository.save(any(Applicant.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(ApplicationException.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }
}
