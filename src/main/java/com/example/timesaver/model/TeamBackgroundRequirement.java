package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "team_background_requirements", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "background_code"}))
@Data
public class TeamBackgroundRequirement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "background_code", nullable = false)
    private String backgroundCode;

    @Column(name = "min_needed", nullable = false)
    private Integer minNeeded;

    @Column(name = "max_needed", nullable = false)
    private Integer maxNeeded;
}