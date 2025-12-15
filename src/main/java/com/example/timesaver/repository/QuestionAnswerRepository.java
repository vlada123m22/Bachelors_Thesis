package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    List<QuestionAnswer> findByApplicant(Applicant applicant);

    List<QuestionAnswer> findByQuestion(FormQuestion question);

    Optional<QuestionAnswer> findByApplicantAndQuestion(Applicant applicant, FormQuestion question);
}