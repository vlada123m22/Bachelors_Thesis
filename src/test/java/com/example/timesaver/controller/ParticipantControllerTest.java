package com.example.timesaver.controller;

import com.example.timesaver.model.dto.participant.ParticipantProjectStatusDTO;
import com.example.timesaver.service.ParticipantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantControllerTest {

    @Mock private ParticipantService participantService;
    @InjectMocks private ParticipantController participantController;

    @Test
    public void testGetMyApplicationsSuccess() {
        List<ParticipantProjectStatusDTO> list = Collections.emptyList();
        when(participantService.getParticipantApplications("user")).thenReturn(list);

        ResponseEntity<?> response = participantController.getMyApplications("user");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
    }

    @Test
    public void testGetMyApplicationsException() {
        when(participantService.getParticipantApplications("user")).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = participantController.getMyApplications("user");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsSuccess() {
        when(participantService.getTeamApplicationsForProject("user", 1))
                .thenAnswer(invocation -> ResponseEntity.ok(Collections.emptyList()));
        ResponseEntity<?> response = participantController.getTeamApplications(1, "user");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetTeamApplicationsException() {
        when(participantService.getTeamApplicationsForProject("user", 1)).thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = participantController.getTeamApplications(1, "user");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}