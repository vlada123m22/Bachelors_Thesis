package com.example.timesaver.model.dto.applicantsdisplay;

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

    private List<QuestionAnswerDTO> questionAnswerDTOs;
}
