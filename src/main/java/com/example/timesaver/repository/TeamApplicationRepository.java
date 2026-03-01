package com.example.timesaver.repository;

import com.example.timesaver.model.Team;
import com.example.timesaver.model.TeamApplication;
import com.example.timesaver.model.TeamApplication.Status;
import com.example.timesaver.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    Optional<TeamApplication> findByTeamAndApplicant(Team team, Applicant applicant);
    List<TeamApplication> findByTeamAndStatus(Team team, Status status);
}