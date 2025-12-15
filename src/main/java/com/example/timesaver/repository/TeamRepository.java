package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTeamNameAndProject(String teamName, Project project);

    @Query("SELECT t FROM Team t WHERE LOWER(t.teamName) = LOWER(:teamName) AND t.project = :project")
    Optional<Team> findByTeamNameIgnoreCaseAndProject(
            @Param("teamName") String teamName,
            @Param("project") Project project
    );

    List<Team> findByProject(Project project);
}