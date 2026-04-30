package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    @Column(nullable = false, name = "project_name")
    private String projectName;

    @Column(name = "project_description")
    private String projectDescription;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Column(name = "max_nr_participants")
    private Integer maxNrParticipants;

    @Column(name = "min_nr_participants")
    private Integer minNrParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "teams_preformed")
    @ColumnDefault("true")
    private Boolean teamsPreformed;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_visibility")
    private ScheduleVisibility scheduleVisibility = ScheduleVisibility.EVERYBODY;

    @Column(name = "roles_options", length = 1000)
    private String rolesOptions; // Pipe-separated organizer options

    @Column(name = "background_options", length = 1000)
    private String backgroundOptions; // Pipe-separated organizer options

    @Column(name = "roles_question_text", length = 500)
    private String rolesQuestionText = "What are your preferred roles in the team?";

    @Column(name = "background_question_text", length = 500)
    private String backgroundQuestionText = "What is your background?";


    public Project(String backgroundOptions, String rolesOptions) {
        this.backgroundOptions = backgroundOptions;
        this.rolesOptions = rolesOptions;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}