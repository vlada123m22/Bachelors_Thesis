package com.example.timesaver.model.dto.applicantsdisplay;

import com.example.timesaver.model.dto.application.TeammateDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    private String teamName;
    private List<TeammateDTO> teamMembers;
}
