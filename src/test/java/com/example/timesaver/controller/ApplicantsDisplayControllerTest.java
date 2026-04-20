package com.example.timesaver.controller;

import com.example.timesaver.model.dto.applicants.display.GetParticipantsDTO;
import com.example.timesaver.model.dto.applicants.display.GetTeamsDTO;
import com.example.timesaver.model.dto.applicants.selection.BulkUpdateSelectionRequest;
import com.example.timesaver.model.dto.applicants.selection.UpdateSelectionRequest;
import com.example.timesaver.model.dto.applicants.selection.UpdateSelectionResponse;
import com.example.timesaver.service.ApplicantSelectionService;
import com.example.timesaver.service.ApplicantsDisplayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicantsDisplayControllerTest {

    @Mock
    private ApplicantsDisplayService displayService;

    @Mock
    private ApplicantSelectionService selectionService;

    @InjectMocks
    private ApplicantsDisplayController controller;

    @Test
    public void testDisplayTeamsSuccess() {
        GetTeamsDTO dto = new GetTeamsDTO(Collections.emptyList(), Collections.emptyList());
        when(displayService.getTeams(1L)).thenReturn(dto);

        ResponseEntity<GetTeamsDTO> response = controller.displayTeams(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    public void testDisplayTeamsFailure() {
        when(displayService.getTeams(1L)).thenThrow(new RuntimeException("Error"));
        ResponseEntity<GetTeamsDTO> response = controller.displayTeams(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDisplayApplicantsSuccess() {
        GetParticipantsDTO dto = new GetParticipantsDTO(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(displayService.getParticipants(1L)).thenReturn(dto);

        ResponseEntity<GetParticipantsDTO> response = controller.displayApplicants(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDisplayApplicantsFailure() {
        when(displayService.getParticipants(1L)).thenThrow(new RuntimeException("Error"));
        ResponseEntity<GetParticipantsDTO> response = controller.displayApplicants(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testUpdateSelectionSuccess() {
        UpdateSelectionRequest req = new UpdateSelectionRequest();
        req.setSelected(true);
        
        ResponseEntity<UpdateSelectionResponse> response = controller.updateSelection(1L, 10L, req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(selectionService).setApplicantSelection(1L, 10L, true);
    }

    @Test
    public void testUpdateSelectionSecurityError() {
        doThrow(new SecurityException("Denied")).when(selectionService).setApplicantSelection(anyLong(), anyLong(), anyBoolean());
        
        ResponseEntity<UpdateSelectionResponse> response = controller.updateSelection(1L, 10L, new UpdateSelectionRequest());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateSelectionIllegalArgument() {
        doThrow(new IllegalArgumentException("Invalid")).when(selectionService).setApplicantSelection(anyLong(), anyLong(), anyBoolean());
        
        ResponseEntity<UpdateSelectionResponse> response = controller.updateSelection(1L, 10L, new UpdateSelectionRequest());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testUpdateSelectionUnexpectedError() {
        doThrow(new RuntimeException("Oops")).when(selectionService).setApplicantSelection(anyLong(), anyLong(), anyBoolean());
        
        ResponseEntity<UpdateSelectionResponse> response = controller.updateSelection(1L, 10L, new UpdateSelectionRequest());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testBulkUpdateSelectionSuccess() {
        BulkUpdateSelectionRequest req = new BulkUpdateSelectionRequest();
        req.setSelected(true);
        req.setApplicantIds(Collections.emptyList());
        
        ResponseEntity<UpdateSelectionResponse> response = controller.bulkUpdateSelection(1L, req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testBulkUpdateSelectionSecurityError() {
        when(selectionService.bulkSetSelection(anyLong(), any(), anyBoolean()))
                .thenThrow(new SecurityException("Denied"));
        
        ResponseEntity<UpdateSelectionResponse> response = controller.bulkUpdateSelection(1L, new BulkUpdateSelectionRequest());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testBulkUpdateSelectionIllegalArgument() {
        when(selectionService.bulkSetSelection(anyLong(), any(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Invalid"));
        
        ResponseEntity<UpdateSelectionResponse> response = controller.bulkUpdateSelection(1L, new BulkUpdateSelectionRequest());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBulkUpdateSelectionUnexpectedError() {
        when(selectionService.bulkSetSelection(anyLong(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Oops"));
        
        ResponseEntity<UpdateSelectionResponse> response = controller.bulkUpdateSelection(1L, new BulkUpdateSelectionRequest());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
