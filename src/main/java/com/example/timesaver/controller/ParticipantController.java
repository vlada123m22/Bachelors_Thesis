package com.example.timesaver.controller;

import com.example.timesaver.model.User;
import com.example.timesaver.model.dto.participant.ParticipantProjectStatusDTO;
import com.example.timesaver.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/participant")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    /**
     * Get all projects the participant has applied for, along with acceptance status
     * GET /participant/my-applications
     */
    @GetMapping("/my-applications")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ADMIN')")
    public ResponseEntity<?> getMyApplications(@RequestAttribute("userName") String userName) {
        try {
            List<ParticipantProjectStatusDTO> applications = participantService.getParticipantApplications(userName);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching applications: " + e.getMessage());
        }
    }

    /**
     * Get team applications for a specific project (only if teams_preformed=false)
     * GET /participant/team-applications/{projectId}
     */
    @GetMapping("/team-applications/{projectId}/{userName}")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ADMIN')")
    public ResponseEntity<?> getTeamApplications(@PathVariable Integer projectId,
                                                   @PathVariable String userName) {
        try {
            return participantService.getTeamApplicationsForProject(userName, projectId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching team applications: " + e.getMessage());
        }
    }
}
