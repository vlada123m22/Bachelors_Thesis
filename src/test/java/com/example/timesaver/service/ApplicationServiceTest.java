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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock private ProjectRepository projectRepository;
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
        User user = new User();
        user.setId(1);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
    }

    private List<ApplicantTeam> makeResult(int applicantId, Integer teamId) {
        return List.of(new ApplicantTeam(applicantId, teamId));
    }

    @Test
    public void testGetFormForProject() {
        Integer projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        when(projectRepository.getBackgroundsRolesByProjectId(projectId)).thenReturn(Optional.of(project));

        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestion("How are you?");
        q.setQuestionType(QuestionType.TEXT);
        when(questionRepository.findByProjectIdFormRetrieval(projectId)).thenReturn(List.of(q));

        GetFormResponse result = applicationService.getFormForProject(projectId);
        assertEquals(1, result.getFormQuestions().size());
        assertEquals("How are you?", result.getFormQuestions().get(0).getQuestion());
    }

    @Test
    public void testGetFormForProjectNotFound() {
        when(projectRepository.getBackgroundsRolesByProjectId(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> applicationService.getFormForProject(1));
    }

    @Test
    public void testSubmitApplicationSuccess() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setJoinExistentTeam(false);
        req.setQuestionsAnswers(Collections.emptyList());

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(42, null));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertEquals("Success", resp.getStatus());
    }

    @Test
    public void testSubmitApplicationProjectNotFound() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(99);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setQuestionsAnswers(Collections.emptyList());

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(0, null));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertTrue(resp.getStatus().startsWith("404"));
    }

    @Test
    public void testSubmitApplicationUnauthorized() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setQuestionsAnswers(Collections.emptyList());

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(-1, null));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertTrue(resp.getStatus().startsWith("401"));
    }

    @Test
    public void testSubmitApplicationTeamAlreadyExists() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTeamName("Cool Team");
        req.setJoinExistentTeam(false);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setQuestionsAnswers(Collections.emptyList());

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(-2, null));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertTrue(resp.getStatus().startsWith("409"));
        assertTrue(resp.getTeamExists());
    }

    @Test
    public void testSubmitApplicationInvalidRolesOrBackgrounds() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Manager"));
        req.setBackground(List.of("CS"));
        req.setQuestionsAnswers(Collections.emptyList());

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(-3, null));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertTrue(resp.getStatus().startsWith("400"));
    }

    @Test
    public void testSubmitApplicationThrowsOnInvalidTimezone() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("Invalid/Zone");

        assertThrows(Exception.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationThrowsWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);

        assertThrows(Exception.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationQuestionsHandling() throws IOException {
        mockUser("test");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setJoinExistentTeam(false);

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

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(100, null));

        FormQuestion qText = new FormQuestion();
        qText.setQuestionNumber(1);
        qText.setQuestionType(QuestionType.TEXT);
        qText.setQuestion("Name?");

        FormQuestion qFile = new FormQuestion();
        qFile.setQuestionNumber(2);
        qFile.setQuestionType(QuestionType.FILE);
        qFile.setQuestion("CV?");

        when(questionRepository.findByProjectIdFormSubmission(1)).thenReturn(List.of(qText, qFile));

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
    public void testSubmitApplicationQuestionMismatch() {
        mockUser("test");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("A");
        req.setLastName("B");

        QuestionAnswerDTO a = new QuestionAnswerDTO();
        a.setQuestionNumber(1);
        a.setQuestionType(QuestionType.TEXT);
        a.setQuestion("Wrong Question");
        req.setQuestionsAnswers(List.of(a));

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(1, null));

        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestionType(QuestionType.TEXT);
        q.setQuestion("Actual Question");
        when(questionRepository.findByProjectIdFormSubmission(1)).thenReturn(List.of(q));

        assertThrows(ApplicationException.class, () -> applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationWithTeammatesSuccess() {
        mockUser("testuser");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setTeamName("Cool Team");
        req.setJoinExistentTeam(false);
        req.setQuestionsAnswers(Collections.emptyList());

        TeammateDTO teammate = new TeammateDTO();
        teammate.setFirstName("Jane");
        teammate.setLastName("Doe");
        req.setTeammates(List.of(teammate));

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(42, 10));
        when(applicantRepository.getIdByNameAndProjectAndTeamId("Jane", "Doe", 1, 10))
                .thenReturn(Optional.empty());

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertEquals("Success", resp.getStatus());
        verify(applicantRepository).insertApplicant(eq("Jane"), eq("Doe"), eq(1), eq(10), any(), any());
    }

    @Test
    public void testSubmitApplicationCheckboxQuestion() {
        mockUser("test");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("A"); req.setLastName("B");
        req.setJoinExistentTeam(false);

        QuestionAnswerDTO a = new QuestionAnswerDTO();
        a.setQuestionNumber(1);
        a.setQuestionType(QuestionType.CHECKBOX);
        a.setQuestion("Pick one?");
        a.setAnswer("Option1");
        req.setQuestionsAnswers(List.of(a));

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(42, null));

        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestionType(QuestionType.CHECKBOX);
        q.setQuestion("Pick one?");
        when(questionRepository.findByProjectIdFormSubmission(1)).thenReturn(List.of(q));

        ApplicationResponse resp = applicationService.submitApplication(req, Collections.emptyMap());
        assertEquals("Success", resp.getStatus());
        verify(questionAnswerRepository).save(any(QuestionAnswer.class));
    }

    @Test
    public void testSubmitApplicationMissingFileThrows() {
        mockUser("test");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("A"); req.setLastName("B");
        req.setJoinExistentTeam(false);

        QuestionAnswerDTO a = new QuestionAnswerDTO();
        a.setQuestionNumber(1);
        a.setQuestionType(QuestionType.FILE);
        a.setQuestion("Upload?");
        req.setQuestionsAnswers(List.of(a));

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(42, null));

        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestionType(QuestionType.FILE);
        q.setQuestion("Upload?");
        when(questionRepository.findByProjectIdFormSubmission(1)).thenReturn(List.of(q));

        assertThrows(ApplicationException.class, () ->
                applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationTypeMismatchThrows() {
        mockUser("test");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("A"); req.setLastName("B");
        req.setJoinExistentTeam(false);

        QuestionAnswerDTO a = new QuestionAnswerDTO();
        a.setQuestionNumber(1);
        a.setQuestionType(QuestionType.TEXT); // sent as TEXT
        a.setQuestion("Name?");
        a.setAnswer("John");
        req.setQuestionsAnswers(List.of(a));

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(42, null));

        FormQuestion q = new FormQuestion();
        q.setQuestionNumber(1);
        q.setQuestionType(QuestionType.CHECKBOX); // stored as CHECKBOX — mismatch
        q.setQuestion("Name?");
        when(questionRepository.findByProjectIdFormSubmission(1)).thenReturn(List.of(q));

        assertThrows(ApplicationException.class, () ->
                applicationService.submitApplication(req, Collections.emptyMap()));
    }

    @Test
    public void testSubmitApplicationInvalidQuestionNumber() {
        mockUser("test");
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setProjectId(1);
        req.setTimezone("UTC");
        req.setRoles(List.of("Dev"));
        req.setBackground(List.of("CS"));
        req.setFirstName("A"); req.setLastName("B");
        req.setJoinExistentTeam(false);

        QuestionAnswerDTO a = new QuestionAnswerDTO();
        a.setQuestionNumber(99); // doesn't exist
        a.setQuestionType(QuestionType.TEXT);
        a.setQuestion("?");
        req.setQuestionsAnswers(List.of(a));

        when(applicantRepository.saveApplicant(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(makeResult(42, null));

        when(questionRepository.findByProjectIdFormSubmission(1)).thenReturn(Collections.emptyList());

        assertThrows(ApplicationException.class, () ->
                applicationService.submitApplication(req, Collections.emptyMap()));
    }
}