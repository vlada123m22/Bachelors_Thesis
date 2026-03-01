package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Entity
@Table(name = "applicants")
@Data
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicantId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private Boolean hasApplied = false;

    @Column(nullable = false)
    private Boolean isSelected = false;

    @Column(nullable = false)
    private ZonedDateTime registrationTimestamp;

    // Timezone information for reference
    @Column(nullable = false)
    private String timezone;

    @Column(name = "roles", length = 1000)
    private String roles; // Pipe-separated applicant selections

    @Column(name = "background", length = 1000)
    private String background; // Pipe-separated applicant selections


    // Inside the Applicant class, add the user field:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        if (registrationTimestamp == null) {
            registrationTimestamp = ZonedDateTime.now();
        }
        if (timezone == null) {
            timezone = registrationTimestamp.getZone().getId();
        }
    }
}