package com.example.timesaver.repository;

import com.example.timesaver.model.Project;
import com.example.timesaver.model.ProjectSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Schedules;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ProjectSchedule, Long> {

    List<ProjectSchedule> findByProjectProjectId(Long projectId);

    List<ProjectSchedule> findByProjectProjectIdAndDayNumber(Long projectId, Integer dayNumber);
}

