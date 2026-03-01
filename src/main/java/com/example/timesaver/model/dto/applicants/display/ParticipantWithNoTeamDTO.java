package com.example.timesaver.model.dto.applicants.display;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantWithNoTeamDTO {

    private String firstName;
    private String lastName;
    private Boolean isSelected;

    private List<QuestionAnswerDTO> questionAnswerDTOs;
}
