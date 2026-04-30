package com.example.timesaver.repository;

import com.example.timesaver.model.Team;
import com.example.timesaver.model.TeamBackgroundRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamBackgroundRequirementRepository extends JpaRepository<TeamBackgroundRequirement, Integer> {
    List<TeamBackgroundRequirement> findByTeam(Team team);
}