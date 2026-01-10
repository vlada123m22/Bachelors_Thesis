package com.example.timesaver.model.dto.applicantsdisplay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetParticipantsDTO {
    List<QuestionDTO> questionDTOs;
    List<ParticipantWithTeamDTO> participantsWithTeams;
    List<ParticipantWithNoTeamDTO> participantsWithoutTeams;
}
