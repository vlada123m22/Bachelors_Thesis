package com.example.timesaver.model.dto.project;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class ScheduleDTO {
    private Long scheduleId;
    private Integer dayNumber;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String activityTitle;
    private String activityDescription;
}