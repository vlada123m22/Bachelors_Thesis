package com.example.timesaver.repository;

import com.example.timesaver.model.Project;
import com.example.timesaver.model.ProjectSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Schedules;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ProjectSchedule, Integer> {

    List<ProjectSchedule> findByProjectProjectId(Integer projectId);

    List<ProjectSchedule> findByProjectProjectIdAndDayNumber(Integer projectId, Integer dayNumber);
}

