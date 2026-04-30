package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "team_member_backgrounds")
@Data
public class TeamMemberBackground {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private TeamMember member;

    @Column(name = "background_code", nullable = false)
    private String backgroundCode;
}