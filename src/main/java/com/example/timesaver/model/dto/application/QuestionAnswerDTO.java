package com.example.timesaver.model.dto.application;

import com.example.timesaver.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionAnswerDTO {

    @NotNull(message = "Question number is required")
    private Integer questionNumber;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @NotBlank(message = "Question text is required")
    private String question;

    // Answer can be empty for file type (file sent separately)
    private String answer;


}