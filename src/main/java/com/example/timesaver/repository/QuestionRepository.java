package com.example.timesaver.repository;

import com.example.timesaver.model.FormQuestion;
import com.example.timesaver.model.dto.applicantsdisplay.QuestionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuestionRepository extends JpaRepository<FormQuestion, Long> {

    @Query("SELECT new com.example.timesaver.model.dto.applicantsdisplay.QuestionDTO(fq.questionNumber, fq.question) FROM FormQuestion fq WHERE fq.project.projectId = :projectId")
     List<QuestionDTO> findQuestionByProjectId (@Param("projectId") Long projectId);

    @Query("SELECT fq FROM FormQuestion fq WHERE fq.project.projectId = :projectId")
    List<FormQuestion> findByProjectId (@Param("projectId") Long projectId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FormQuestion fq WHERE fq.project.projectId = :projectId")
    void deleteQuestions (@Param("projectId") Long projectId);


}
