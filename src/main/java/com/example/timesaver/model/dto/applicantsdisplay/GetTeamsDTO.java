package com.example.timesaver.model.dto.applicantsdisplay;

import com.example.timesaver.model.dto.application.TeammateDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTeamsDTO {
    private List<TeamDTO> teams;
    private List<TeammateDTO> singleParticipants;
}
