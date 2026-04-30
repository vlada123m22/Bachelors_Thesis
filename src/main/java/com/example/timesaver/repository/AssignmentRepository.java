package com.example.timesaver.repository;

import com.example.timesaver.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByProjectProjectId(Integer projectId);
}