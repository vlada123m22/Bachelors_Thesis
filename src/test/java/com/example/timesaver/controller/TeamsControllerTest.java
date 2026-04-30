package com.example.timesaver.controller;

import com.example.timesaver.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TeamsControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamsController teamsController;

    @Test
    public void testCreateProjectSuccess() {
        ResponseEntity<Void> response = teamsController.createProject(1);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(teamService).createTeams(1);
    }

    @Test
    public void testCreateProjectFailure() {
        doThrow(new RuntimeException("Error")).when(teamService).createTeams(1);
        ResponseEntity<Void> response = teamsController.createProject(1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}