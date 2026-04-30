package com.example.timesaver.repository;

import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.application.ApplicantTeam;
import com.example.timesaver.model.dto.project.GetProjectResponse;
import com.example.timesaver.model.dto.project.ProjectDashboardDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    // Find all projects by organizer
    @Query("SELECT new com.example.timesaver.model.dto.project.GetProjectResponse( p.projectId, p.projectName, p.projectDescription, p.startDate, p.endDate, p.maxNrParticipants, p.minNrParticipants)  " +
            "FROM Project p " +
            "WHERE p.organizer = :organizer")
    List<GetProjectResponse> findMainProjInfoByOrganizer(User organizer);

    // Check if project exists and belongs to organizer
    boolean existsByProjectIdAndOrganizer(Integer projectId, User organizer);

    // Get future projects for dashboard
    @Query("SELECT new com.example.timesaver.model.dto.project.ProjectDashboardDTO(" +
            "p.projectId, p.projectName, p.startDate, p.endDate, p.projectDescription, " +
            "p.maxNrParticipants, p.minNrParticipants) " +
            "FROM Project p " +
            "WHERE p.startDate > CURRENT_TIMESTAMP " +
            "ORDER BY p.startDate ASC")
    List<ProjectDashboardDTO> findFutureProjects();

    @Query("SELECT new com.example.timesaver.model.Project(p.backgroundOptions, p.rolesOptions) " +
            " FROM Project p " +
            "WHERE p.projectId = :projectId")
    Optional<Project> getBackgroundsRolesByProjectId(@Param("projectId") Integer projectId);

    //Note: Returns a single value, not a list
    @Query(
            value = "SELECT * FROM delete_project(" +
                    "CAST(:p_project_id AS integer), " +
                    "CAST(:p_user_id AS integer) )" ,
            nativeQuery = true
    )
    Optional<String> deleteProjectById(
            @Param("p_project_id") Integer projectId,
            @Param("p_user_id") Integer userId
    );

}