package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_name", "project_id"})
})
@Data
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    @Column(nullable = false)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private ZonedDateTime dateCreated;

    @Column(nullable = false)
    private ZonedDateTime dateUpdated;


    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        dateCreated = now;
        dateUpdated = now;
    }

    @PreUpdate
    protected void onUpdate() {
        dateUpdated = ZonedDateTime.now();
    }
}