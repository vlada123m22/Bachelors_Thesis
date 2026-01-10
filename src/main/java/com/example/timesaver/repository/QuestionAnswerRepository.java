package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.applicantsdisplay.QuestionAnswerDTO;
import com.example.timesaver.model.dto.applicantsdisplay.QuestionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
    @Query("SELECT new com.example.timesaver.model.dto.applicantsdisplay.QuestionAnswerDTO(qa.question.questionNumber, qa.questionAnswer)  FROM QuestionAnswer qa WHERE qa.applicant.applicantId = :applicantId")
    List<QuestionAnswerDTO> findQuestionNumberAndAnswerByApplicantId(@Param("applicantId") Long applicantId );
}