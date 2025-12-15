package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


// ==================== APPLICANT REPOSITORY ====================
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    Optional<Applicant> findByFirstNameAndLastNameAndProjectAndTeam(
            String firstName, String lastName, Project project, Team team
    );

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

    List<Applicant> findByProject(Project project);

    List<Applicant> findByTeam(Team team);

    boolean existsByFirstNameAndLastNameAndProject(
            String firstName, String lastName, Project project
    );
}
