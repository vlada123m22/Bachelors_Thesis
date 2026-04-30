package com.example.timesaver.repository;

import com.example.timesaver.model.FormQuestion;
import com.example.timesaver.model.dto.applicants.display.QuestionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface QuestionRepository extends JpaRepository<FormQuestion, Integer> {

    @Query("SELECT new com.example.timesaver.model.dto.applicants.display.QuestionDTO(fq.questionNumber, fq.question) FROM FormQuestion fq WHERE fq.project.projectId = :projectId")
     List<QuestionDTO> findQuestionByProjectId (@Param("projectId") Integer projectId);

    @Query("SELECT new com.example.timesaver.model.FormQuestion(fq.questionNumber, fq.questionType, fq.question, fq.checkboxOptions) " +
            "FROM FormQuestion fq " +
            "WHERE fq.project.projectId = :projectId")
    List<FormQuestion> findByProjectId (@Param("projectId") Integer projectId);

    @Query("SELECT new com.example.timesaver.model.FormQuestion( fq.questionId, fq.questionNumber, fq.questionType, fq.question) " +
            "FROM FormQuestion fq " +
            "WHERE fq.project.projectId = :projectId")
    List<FormQuestion> findByProjectIdFormSubmission (@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FormQuestion fq WHERE fq.project.projectId = :projectId")
    void deleteQuestions (@Param("projectId") Integer projectId);


    @Query("SELECT new com.example.timesaver.model.FormQuestion(fq.questionNumber, fq.questionType, fq.question, fq.checkboxOptions) " +
            "FROM FormQuestion fq " +
            "WHERE fq.project.projectId = :projectId")
    List<FormQuestion> findByProjectIdFormRetrieval(@Param("projectId") Integer projectId);
}
