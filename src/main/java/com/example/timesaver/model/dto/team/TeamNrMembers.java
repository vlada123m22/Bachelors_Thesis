package com.example.timesaver.model.dto.team;

import com.example.timesaver.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamNrMembers {
    private Team team;
    private Long nrOfMembers;
}
