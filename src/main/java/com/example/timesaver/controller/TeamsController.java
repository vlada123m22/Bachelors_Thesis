package com.example.timesaver.controller;

import com.example.timesaver.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeamsController {

    @Autowired
    private TeamService teamService;

    @PostMapping ("/{projectId}/create-teams")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<Void> createProject(@PathVariable Long projectId) {
        try {
            teamService.createTeams(projectId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }   catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
