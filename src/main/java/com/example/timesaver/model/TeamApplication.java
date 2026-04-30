package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Entity
@Table(name = "team_applications",
      uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "applicant_id"}))
@Data
public class TeamApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamApplicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "applied_at", nullable = false)
    private ZonedDateTime appliedAt;

    @Column(name = "decision_at")
    private ZonedDateTime decisionAt;

    public enum Status { PENDING, ACCEPTED, REJECTED, WITHDRAWN }

    @PrePersist
    void onCreate() { this.appliedAt = ZonedDateTime.now(); }
}