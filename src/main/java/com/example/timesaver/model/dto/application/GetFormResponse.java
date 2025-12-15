package com.example.timesaver.model.dto.application;


import com.example.timesaver.model.dto.project.FormQuestionDTO;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@Getter
public class GetFormResponse {
    private List<FormQuestionDTO> formQuestions;

    public GetFormResponse(List<FormQuestionDTO> formQuestions) {
        this.formQuestions = formQuestions;
    }

}