package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.team.TeamNrMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface TeamRepository extends JpaRepository<Team, Integer> {

    Optional<Team> findByTeamNameAndProject(String teamName, Project project);

    @Query("SELECT t FROM Team t WHERE LOWER(t.teamName) = LOWER(:teamName) AND t.project = :project")
    Optional<Team> findByTeamNameIgnoreCaseAndProject(
            @Param("teamName") String teamName,
            @Param("project") Project project
    );

    @Query("SELECT new com.example.timesaver.model.dto.team.TeamNrMembers(a.team, CAST(COUNT(a.applicantId) AS integer)) " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "GROUP BY a.team " +
            "HAVING COUNT(a.applicantId) < :minParticipants ")
    List<TeamNrMembers> incompleteTeamsByProject(
            @Param("projectId") Integer projectId,
            @Param("minParticipants") Integer minParticipants
    );

    @Query("SELECT t " +
            "FROM Team t " +
            "WHERE t.project.projectId = :projectId ")
    List<Team> findAllTeamsByProject(
            @Param("projectId") Integer projectId
    );


    @Modifying
    @Transactional
    @Query("UPDATE Applicant a " +
            "SET a.team = :team1 " +
            "WHERE a.team = :team2 ")
    void joinTeams(@Param("team1") Team team1, @Param("team2") Team team2);


}