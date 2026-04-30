package com.example.timesaver.repository;

import com.example.timesaver.model.*;
import com.example.timesaver.model.dto.applicants.display.GetParticipantsHelperDTO;
import com.example.timesaver.model.dto.application.ApplicantTeam;
import com.example.timesaver.model.dto.application.TeammateDTO;
import com.example.timesaver.model.dto.participant.ParticipantProjectStatusDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface ApplicantRepository extends JpaRepository<Applicant, Integer> {

    @NativeQuery("SELECT applicatnt_id FROM applicants a " +
            "WHERE LOWER(a.firstName) = LOWER(:firstName) AND " +
            "LOWER(a.lastName) = LOWER(:lastName) AND " +
            "a.project_id = :project AND " +
            "a.team_id = :teamId")
    Optional<Integer> getIdByNameAndProjectAndTeamId(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("project") Integer projectId,
            @Param("team") Integer teamId
    );

    @NativeQuery ("INSERT INTO applicants ( first_name, last_name, project_id, team_id, has_applied, is_selected, registration_timestamp, timezone) " +
                    "VALUES ( :firstName, :lastName, :projectId, :teamId, 'false', 'false', :registrationTimestamp, :timezone)")
    void insertApplicant(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("projectId") Integer projectId, @Param("teamId") Integer teamId, @Param("registrationTimestamp") ZonedDateTime registrationTimestamp, @Param("timezone") String timezone);

    @Query("SELECT new com.example.timesaver.model.dto.applicants.display.GetParticipantsHelperDTO (a.applicantId, a.firstName, a.lastName, a.team.teamName, a.isSelected)  " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId AND a.team IS NULL")
    List<Applicant> getSingleApplicants (@Param("projectId") Integer projectId);

    @Query("SELECT new com.example.timesaver.model.dto.application.TeammateDTO( a.firstName, a.lastName) " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team IS NULL")
    List<TeammateDTO> getSingleApplicantsFirstAndLastName (@Param("projectId") Integer projectId);

    @Query("SELECT new com.example.timesaver.model.dto.applicants.display.GetParticipantsHelperDTO (a.applicantId, a.firstName, a.lastName, a.team.teamName, a.isSelected) " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team IS NOT NULL")
    List<GetParticipantsHelperDTO> getApplicantsWithTeam(@Param("projectId") Integer projectId);

    @Query("SELECT new com.example.timesaver.model.dto.applicants.display.GetParticipantsHelperDTO (a.applicantId, a.firstName, a.lastName, a.team.teamName, a.isSelected)  " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team IS NULL")
    List<GetParticipantsHelperDTO> getApplicantsWithNoTeam(@Param("projectId") Integer projectId);

    @Query("SELECT new com.example.timesaver.model.dto.application.TeammateDTO( a.firstName, a.lastName) " +
            "FROM Applicant a " +
            "WHERE a.project.projectId = :projectId " +
            "AND a.team.teamId = :teamId")
    List<TeammateDTO> getFirstAndLastNameByTeam(@Param("projectId") Integer projectId, @Param("teamId") Integer teamId);

    @Query("SELECT a FROM Applicant a WHERE a.user = :user AND a.project = :project")
    Optional<Applicant> findByUserAndProject(User user, Project project);

    @Query("SELECT a.applicantId FROM Applicant a WHERE a.user = :user AND a.project = :project")
    Optional<Integer> findIdByUserAndProject(User user, Project project);

    @Query("SELECT a.isSelected FROM Applicant a WHERE a.user = :user AND a.project = :project")
    Optional<Boolean> getIsSelectedByUserAndProject(User user, Project project);

    @Query("SELECT new com.example.timesaver.model.dto.participant.ParticipantProjectStatusDTO(" +
            "p.projectId, p.projectName, p.startDate, p.endDate, p.projectDescription, " +
            "a.hasApplied, a.isSelected) " +
            "FROM Applicant a " +
            "JOIN a.project p " +
            "WHERE a.user = :user")
    List<ParticipantProjectStatusDTO> findProjectStatusByUser(User user);


    @Query(
            value = "SELECT * FROM save_applicant(" +
                    "CAST(:in_user_id AS integer), " +
                    "CAST(:in_project_id AS integer), " +
                    ":in_first_name, :in_last_name, :in_team_name, " +
                    "CAST(:in_join_existent_team AS boolean), " +
                    ":in_time_zone, CAST(:date_submitted AS timestamp with time zone), " +
                    ":in_roles, :in_background)",
            nativeQuery = true
    )
    List<ApplicantTeam> saveApplicant(
            @Param("in_user_id") Integer userId,
            @Param("in_project_id") Integer projectId,
            @Param("in_first_name") String firstName,
            @Param("in_last_name") String lastName,
            @Param("in_team_name") String teamName,
            @Param("in_join_existent_team") Boolean joinExistingTeam,
            @Param("in_time_zone") String timeZone,
            @Param("date_submitted") ZonedDateTime registrationTimestamp,
            @Param("in_roles") String roles,
            @Param("in_background") String background
    );
}
