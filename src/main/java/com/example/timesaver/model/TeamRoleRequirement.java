package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "team_role_requirements", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "role_code"}))
@Data
public class TeamRoleRequirement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "role_code", nullable = false)
    private String roleCode;

    @Column(name = "min_needed", nullable = false)
    private Integer minNeeded;

    @Column(name = "max_needed", nullable = false)
    private Integer maxNeeded;
}