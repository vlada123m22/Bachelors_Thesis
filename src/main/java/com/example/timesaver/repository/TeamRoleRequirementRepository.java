package com.example.timesaver.repository;

import com.example.timesaver.model.Team;
import com.example.timesaver.model.TeamRoleRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRoleRequirementRepository extends JpaRepository<TeamRoleRequirement, Integer> {
    List<TeamRoleRequirement> findByTeam(Team team);
}