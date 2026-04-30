package com.example.timesaver.model.dto.application;


import com.example.timesaver.model.dto.project.FormQuestionDTO;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetFormResponse {
    private  List<FormQuestionDTO> formQuestions;
    private  List<String> roleOptions;
    private  List<String> backgroundOptions;

    public GetFormResponse() {}

    public GetFormResponse(List<FormQuestionDTO> formQuestions, List<String> roleOptions, List<String> backgroundOptions) {
        this.formQuestions = formQuestions;
        this.roleOptions = roleOptions;
        this.backgroundOptions = backgroundOptions;
    }

}