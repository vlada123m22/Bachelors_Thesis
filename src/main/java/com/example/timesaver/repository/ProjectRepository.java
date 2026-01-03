package com.example.timesaver.repository;

import com.example.timesaver.model.Project;
import com.example.timesaver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Find all projects by organizer
    List<Project> findByOrganizer(User organizer);

    // Check if project exists and belongs to organizer
    boolean existsByProjectIdAndOrganizer(Long projectId, User organizer);
}