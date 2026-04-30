package com.example.timesaver.service;

import com.example.timesaver.exceptions.ApplicationException;
import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.application.*;
import com.example.timesaver.model.dto.project.FormQuestionDTO;
import com.example.timesaver.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ProjectRepository projectRepository;
    private final ApplicantRepository applicantRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final FileStorageService fileStorageService;
    private final QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    public ApplicationService(ProjectRepository projectRepository, ApplicantRepository applicantRepository, QuestionAnswerRepository questionAnswerRepository, FileStorageService fileStorageService, QuestionRepository questionRepository) {
        this.projectRepository = projectRepository;
        this.applicantRepository = applicantRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.fileStorageService = fileStorageService;
        this.questionRepository = questionRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUserName(username).orElse(null);
    }

    /**
     * Get form questions for a project (public endpoint)
     */
    @Transactional(readOnly = true)
    public GetFormResponse getFormForProject(Integer projectId) {
        GetFormResponse response = new GetFormResponse();
        Optional<Project> projectBackgroundsRolesOpt = projectRepository.getBackgroundsRolesByProjectId(projectId);

        if (projectBackgroundsRolesOpt.isEmpty()){
            throw new RuntimeException("Project not found");
        }

        Project projectBackgroundsRoles = projectBackgroundsRolesOpt.get();
        List<String> allowedRoles = PipeList.split(projectBackgroundsRoles.getRolesOptions());
        List<String> allowedBkg = PipeList.split(projectBackgroundsRoles.getBackgroundOptions());

        response.setBackgroundOptions(allowedBkg);
        response.setRoleOptions(allowedRoles);
/* 1. Lookup project for bckd and roles, by projectId - done
2. Lookup questions by projectID (questionNumber, QuestionType, question, checkboxOptions)
* */



        List<FormQuestionDTO> questionDTOs = questionRepository.findByProjectIdFormRetrieval(projectId).stream()
                .sorted(Comparator.comparing(FormQuestion::getQuestionNumber))
                .map(q -> {
                    FormQuestionDTO dto = new FormQuestionDTO();
                    dto.setQuestionNumber(q.getQuestionNumber());
                    dto.setQuestionType(q.getQuestionType());
                    dto.setQuestion(q.getQuestion());
                    dto.setCheckboxOptions(q.getCheckboxOptions());
                    return dto;
                })
                .collect(Collectors.toList());

        return new GetFormResponse(questionDTOs, allowedRoles, allowedBkg);
    }

    /**
     * Submit application (handles team checking and file uploads)
     */
    @Transactional
    public ApplicationResponse submitApplication(
            SubmitApplicationRequest request,
            Map<Integer, MultipartFile> fileAnswers) {
            Integer userId = Math.toIntExact(getCurrentUser().getId());

            ZoneId timezone = ZoneId.of(request.getTimezone());
            String roles = PipeList.join(request.getRoles());
            String backgrounds = PipeList.join(request.getBackground());
            ZonedDateTime registrationTimestamp = ZonedDateTime.now(timezone);

            //The query in applicantRepository is expected to return one single row
            ApplicantTeam applicantTeam = applicantRepository.saveApplicant(userId, request.getProjectId(),
                    request.getFirstName(), request.getLastName(), request.getTeamName(), request.getJoinExistentTeam(),
                    request.getTimezone(), registrationTimestamp, roles, backgrounds).getFirst();

            Integer applicantId = applicantTeam.getApplicantId();
            Integer teamId = applicantTeam.getTeamId();


            if (applicantId == 0)
                return new ApplicationResponse(
                "404: Not Found",
                "The project with the indicated id was not found"
                );
            else if (applicantId==-1)
                return new ApplicationResponse(
                        "401: Unauthorized",
                        "You must have an account to apply for this project. Please log in or sign up."
                );
            else if(applicantId == -2)
                return new ApplicationResponse(
                        "409: Conflict",
                        "The team already exists. Please join the team or choose a different name.",
                        true
                );
            else if (applicantId == -3)
                return new ApplicationResponse(
                        "400: Bad Request",
                        "This message is meant for the frontend developer. The roles or backgrounds the user has selected are not allowed for this project. Please display on the UI to the user the roles and backgrounds returned by the following endpoint: GET /projects/apply/{projectId}"
                );


        //Process teammates
        if (request.getTeammates() != null && !request.getTeammates().isEmpty() && !Objects.isNull(teamId)) {
            for (TeammateDTO teammateDTO : request.getTeammates()) {
                Optional<Integer> teammateOpt = applicantRepository
                        .getIdByNameAndProjectAndTeamId(
                                teammateDTO.getFirstName(),
                                teammateDTO.getLastName(),
                                request.getProjectId(),
                                teamId
                        );

                if (teammateOpt.isEmpty()) {
                    applicantRepository.insertApplicant(teammateDTO.getFirstName(), teammateDTO.getLastName(), request.getProjectId(), teamId, registrationTimestamp, timezone.getId());
                }
            }
        }

        saveQuestionAnswers(request, applicantId, request.getProjectId(), fileAnswers);

        return new ApplicationResponse(
                "Success",
                "Application submitted successfully"
        );
    }

    private void saveQuestionAnswers(SubmitApplicationRequest request, Integer applicantId, Integer projectId, Map<Integer, MultipartFile> fileAnswers) {

        //TODO only questionId, questionNumber, question and questionType are needed
        // TODO INDEX ALSO NEEDED
        Map<Integer, FormQuestion> questionMap = questionRepository.findByProjectIdFormSubmission(projectId).stream()
                .collect(Collectors.toMap(FormQuestion::getQuestionNumber, q -> q));

        // 7. Save question answers
        for (QuestionAnswerDTO answerDTO : request.getQuestionsAnswers()) {
            FormQuestion question = questionMap.get(answerDTO.getQuestionNumber());
            if (question == null) {
                throw new ApplicationException(
                        "Invalid question number: " + answerDTO.getQuestionNumber()
                );
            }

            // Verify question type matches
            if (!question.getQuestionType().equals(answerDTO.getQuestionType())) {
                throw new ApplicationException(
                        "Question type mismatch for question " + answerDTO.getQuestionNumber()
                );
            }

            // Verify question itself matches
            if (!question.getQuestion().equals(answerDTO.getQuestion())) {
                throw new ApplicationException(
                        "Question text mismatch: question nr. " + answerDTO.getQuestionNumber()
                );
            }

            //TODO Come to this later
            QuestionAnswer  answer = new QuestionAnswer();
            answer.setQuestion(question);
            answer.setApplicant(new Applicant(applicantId));

            // Handle different question types
            switch (question.getQuestionType()) {
                case FILE:
                    MultipartFile file = fileAnswers.get(answerDTO.getQuestionNumber());
                    if (file == null || file.isEmpty()) {
                        throw new ApplicationException(
                                "File not submitted or empty. Question: " + answerDTO.getQuestionNumber()
                        );
                    }
                    try {
                        String filePath = fileStorageService.storeFile(
                                file,
                                projectId,
                                applicantId,
                                answerDTO.getQuestionNumber()
                        );
                        answer.setQuestionAnswer(filePath);
                    } catch (Exception e) {
                        throw new ApplicationException(
                                "Failed to upload file: " + e.getMessage()
                        );
                    }
                    break;

                case CHECKBOX:
                    // Checkbox answers stored as "option1|option2|option3"
                    if (answerDTO.getAnswer() == null || answerDTO.getAnswer().isEmpty()) {
                        throw new ApplicationException(
                                "Answer required for question " + answerDTO.getQuestionNumber()
                        );
                    }
                    answer.setQuestionAnswer(answerDTO.getAnswer());
                    break;

                case TEXT:
                    if (answerDTO.getAnswer() == null || answerDTO.getAnswer().isEmpty()) {
                        throw new ApplicationException(
                                "Answer required for question " + answerDTO.getQuestionNumber()
                        );
                    }
                    answer.setQuestionAnswer(answerDTO.getAnswer());
                    break;
            }

            questionAnswerRepository.save(answer);
        }
    }
    private void validateSubset(List<String> subset, List<String> allowed, String fieldName) {
        for (String s : subset) {
            if (!allowed.contains(s)) {
                throw new ApplicationException(
                        "Invalid " + fieldName + ": " + s
                );
            }
        }
    }
}