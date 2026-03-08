package com.example.timesaver.repository;

import com.example.timesaver.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAssignmentIdAndTeamTeamId(Long assignmentId, Long teamId);

    List<Submission> findByAssignmentId(Long assignmentId);
}