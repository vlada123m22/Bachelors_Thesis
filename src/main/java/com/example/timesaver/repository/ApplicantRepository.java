package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.application.TeammateDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    @Query("SELECT a FROM Applicant a WHERE " +
            "LOWER(a.firstName) = LOWER(:firstName) AND " +
            "LOWER(a.lastName) = LOWER(:lastName) AND " +
            "a.project = :project AND " +
            "a.team = :team")
    Optional<Applicant> findByNameAndProjectAndTeam(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("project") Project project,
            @Param("team") Team team
    );

    @Query("SELECT a FROM Applicant a WHERE a.project.projectId = :projectId AND a.team IS NULL")
    List<Applicant> getSingleApplicants (@Param("projectId") Long projectId);

    @Query("SELECT new com.example.timesaver.model.dto.application.TeammateDTO( a.firstName, a.lastName) " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team IS NULL")
    List<TeammateDTO> getSingleApplicantsFirstAndLastName (@Param("projectId") Long projectId);

    @Query("SELECT a FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team IS NOT NULL")
    List<Applicant> getApplicantsWithTeam(@Param("projectId") Long projectId);

    @Query("SELECT a FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team IS NULL")
    List<Applicant> getApplicantsWithNoTeam(@Param("projectId") Long projectId);

    @Query("SELECT new com.example.timesaver.model.dto.application.TeammateDTO( a.firstName, a.lastName) " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team.teamId = :teamId")
    List<TeammateDTO> getFirstAndLastNameByTeam(@Param("projectId") Long projectId, @Param("teamId") Long teamId);

}
