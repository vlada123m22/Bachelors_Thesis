package com.example.timesaver.model.dto.project;

import com.example.timesaver.model.QuestionType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FormQuestionDTO {

    @NotNull(message = "Question number is required")
    @Min(value = 1, message = "Question number must be at least 1")
    private Integer questionNumber;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @NotBlank(message = "Question text is required")
    @Size(min = 1, max = 1000, message = "Question must be between 1 and 1000 characters")
    private String question;

    @Size(max = 1000)
    private String checkboxOptions;

}