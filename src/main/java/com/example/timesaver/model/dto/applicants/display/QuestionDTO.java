package com.example.timesaver.model.dto.applicants.display;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
    private Integer questionNumber;
    private String question;
}
