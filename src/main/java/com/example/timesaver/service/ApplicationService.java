package com.example.timesaver.service;

import com.example.timesaver.exceptions.ApplicationException;
import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.application.*;
import com.example.timesaver.model.dto.project.FormQuestionDTO;
import com.example.timesaver.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final TeamRepository teamRepository;
    private final ApplicantRepository applicantRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final FileStorageService fileStorageService;
    private final QuestionRepository questionRepository;

    public ApplicationService(ProjectRepository projectRepository, TeamRepository teamRepository, ApplicantRepository applicantRepository, QuestionAnswerRepository questionAnswerRepository, FileStorageService fileStorageService, QuestionRepository questionRepository) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.applicantRepository = applicantRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.fileStorageService = fileStorageService;
        this.questionRepository = questionRepository;
    }

    /**
     * Get form questions for a project (public endpoint)
     */
    @Transactional(readOnly = true)
    public GetFormResponse getFormForProject(Long projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found");
        }

        //Project project = projectOpt.get();

        List<FormQuestionDTO> questionDTOs = questionRepository.findByProjectId(projectId).stream()
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

        return new GetFormResponse(questionDTOs);
    }

    /**
     * Submit application (handles team checking and file uploads)
     */
    @Transactional
    public ApplicationResponse submitApplication(
            SubmitApplicationRequest request,
            Map<Integer, MultipartFile> fileAnswers) {


        // 1. Find project
        Project project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() ->
                        new ApplicationException("Project not found")
                );

            // 2. Validate timezone
            ZoneId timezone;
            try {
                timezone = ZoneId.of(request.getTimezone());
            } catch (Exception e) {
                throw new ApplicationException("Invalid timezone: " + request.getTimezone());
            }

            // 3. Check if team exists (case-insensitive)
            Optional<Team> existingTeamOpt = teamRepository
                    .findByTeamNameIgnoreCaseAndProject(request.getTeamName(), project);

            // CHECK 1: Team exists check
            if (existingTeamOpt.isPresent() && !request.getJoinExistentTeam()) {
                return  new ApplicationResponse("Failure", "Team "+request.getTeamName() + " already exists", Boolean.TRUE);
            }

        if (Objects.isNull(request.getTeamName()) && !Objects.isNull(request.getTeammates()) && !request.getTeammates().isEmpty()) {
            throw new ApplicationException(
                    "A team name must be provided if you are joining with a team!"
            );
        }

        if (!Objects.isNull(request.getTeamName()) && (Objects.isNull(request.getTeammates()) || request.getTeammates().isEmpty())) {
            throw new ApplicationException(
                    "A team must consist of more than 1 member. You must either add a few teammates or leave the team name blank"
            );
        }

        //save the applicant that is joining without a team
        if (Objects.isNull(request.getTeamName())) {
            Applicant applicant = new Applicant();
            applicant.setFirstName(request.getFirstName());
            applicant.setLastName(request.getLastName());
            applicant.setHasApplied(true);
            applicant.setRegistrationTimestamp(ZonedDateTime.now(timezone));
            applicant.setIsSelected(false);
            applicant.setTimezone(timezone.getId());
            applicant.setProject(project);
            applicant.setTeam(null);
            applicantRepository.save(applicant);
//            return new ApplicationResponse(
//                    "Success",
//                    "Application submitted successfully",
//                    applicant.getApplicantId()
//            );
        }

        // 4. Get or create team
        Team team =null;
        if (existingTeamOpt.isPresent()) {
            team = existingTeamOpt.get();
            team.setDateUpdated(ZonedDateTime.now(timezone));
        } else if (!Objects.isNull(request.getTeamName())&&!request.getTeamName().isEmpty()&&!request.getTeamName().isBlank())
        {
            team = new Team();
            team.setTeamName(request.getTeamName());
            team.setProject(project);
            team.setDateCreated(ZonedDateTime.now(timezone));
            team.setDateUpdated(ZonedDateTime.now(timezone));
            team = teamRepository.save(team);
        }
        
            // 5. Process teammates (add them if not exists, with hasApplied = false)
            if (request.getTeammates() != null && !request.getTeammates().isEmpty() && !Objects.isNull(team)) {
                for (TeammateDTO teammateDTO : request.getTeammates()) {
                    Optional<Applicant> teammateOpt = applicantRepository
                            .findByNameAndProjectAndTeam(
                                    teammateDTO.getFirstName(),
                                    teammateDTO.getLastName(),
                                    project,
                                    team
                            );

                    if (teammateOpt.isEmpty()) {
                        Applicant teammate = new Applicant();
                        teammate.setFirstName(teammateDTO.getFirstName());
                        teammate.setLastName(teammateDTO.getLastName());
                        teammate.setProject(project);
                        teammate.setTeam(team);
                        teammate.setHasApplied(false);
                        teammate.setIsSelected(false);
                        teammate.setRegistrationTimestamp(ZonedDateTime.now(timezone));
                        teammate.setTimezone(timezone.getId());
                        applicantRepository.save(teammate);
                    }
                }
            }

            // CHECK 2: Check if applicant was already added by teammates
            Optional<Applicant> existingApplicantOpt = applicantRepository
                    .findByNameAndProjectAndTeam(
                            request.getFirstName(),
                            request.getLastName(),
                            project,
                            team
                    );

            Applicant applicant;
            if (existingApplicantOpt.isPresent()) {
                // Applicant exists, update hasApplied to true
                applicant = existingApplicantOpt.get();
                applicant.setHasApplied(true);
                applicant.setRegistrationTimestamp(ZonedDateTime.now(timezone));
                applicant.setTimezone(timezone.getId());
            } else {
                // Create new applicant
                applicant = new Applicant();
                applicant.setFirstName(request.getFirstName());
                applicant.setLastName(request.getLastName());
                applicant.setProject(project);
                applicant.setTeam(team);
                applicant.setHasApplied(true);
                applicant.setIsSelected(false);
                applicant.setRegistrationTimestamp(ZonedDateTime.now(timezone));
                applicant.setTimezone(timezone.getId());
            }

            applicant = applicantRepository.save(applicant);


            Map<Integer, FormQuestion> questionMap = questionRepository.findByProjectId(project.getProjectId()).stream()
                    .collect(Collectors.toMap(FormQuestion::getQuestionNumber, q -> q));

            //do not need to answer all questions. Later will add a new field for questions: isRequired
            // 6. Validate all questions are answered
//            if (request.getQuestionsAnswers().size() != questionMap.size()) {
//                return new ApplicationResponse("Failure", "All questions must be answered");
//            }



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

                QuestionAnswer  answer = new QuestionAnswer();
                answer.setQuestion(question);
                answer.setApplicant(applicant);

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
                                    project.getProjectId(),
                                    applicant.getApplicantId(),
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

            return new ApplicationResponse(
                    "Success",
                    "Application submitted successfully"
            );
    }
}
