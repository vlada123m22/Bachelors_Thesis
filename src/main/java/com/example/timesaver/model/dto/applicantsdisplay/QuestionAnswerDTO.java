package com.example.timesaver.model.dto.applicantsdisplay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerDTO {
    private Integer questionNumber;
    private String questionAnswer;
}
