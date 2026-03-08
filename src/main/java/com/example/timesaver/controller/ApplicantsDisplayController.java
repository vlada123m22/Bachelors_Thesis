package com.example.timesaver.controller;

import com.example.timesaver.model.dto.applicants.display.GetParticipantsDTO;
import com.example.timesaver.model.dto.applicants.display.GetTeamsDTO;
import com.example.timesaver.model.dto.applicants.selection.BulkUpdateSelectionRequest;
import com.example.timesaver.model.dto.applicants.selection.UpdateSelectionRequest;
import com.example.timesaver.model.dto.applicants.selection.UpdateSelectionResponse;
import com.example.timesaver.service.ApplicantSelectionService;
import com.example.timesaver.service.ApplicantsDisplayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{projectId}")
public class ApplicantsDisplayController {
    private final ApplicantsDisplayService applicantsDisplayService;
    private final ApplicantSelectionService applicantSelectionService;

    public ApplicantsDisplayController(ApplicantsDisplayService applicantsDisplayService, ApplicantSelectionService applicantSelectionService) {
        this.applicantsDisplayService = applicantsDisplayService;
        this.applicantSelectionService = applicantSelectionService;
    }

    @GetMapping("/teams")
    public ResponseEntity<GetTeamsDTO> displayTeams(@PathVariable Long projectId){
        ResponseEntity<GetTeamsDTO> response = null;
        try{
            response =  new ResponseEntity<>(applicantsDisplayService.getTeams(projectId), HttpStatus.OK);
            return response;
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/participants")
    public ResponseEntity<GetParticipantsDTO> displayApplicants(@PathVariable Long projectId){
        ResponseEntity<GetParticipantsDTO> response = null;
        try {
            response=  new ResponseEntity<>(applicantsDisplayService.getParticipants(projectId), HttpStatus.OK);
            return response;
        } catch ( Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/applicants/{applicantId}/selection")
    public ResponseEntity<UpdateSelectionResponse> updateSelection(
            @PathVariable Long projectId,
            @PathVariable Long applicantId,
            @RequestBody UpdateSelectionRequest request
    ) {
        try {
            boolean selected = Boolean.TRUE.equals(request.getSelected());
            applicantSelectionService.setApplicantSelection(projectId, applicantId, selected);
            String msg = selected ? "Applicant marked as accepted" : "Applicant marked as rejected";
            return ResponseEntity.ok(new UpdateSelectionResponse("OK", msg));
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UpdateSelectionResponse("ERROR", se.getMessage()));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateSelectionResponse("ERROR", iae.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UpdateSelectionResponse("ERROR", "Unexpected error"));
        }
    }

    @PatchMapping("/applicants/selection")
    public ResponseEntity<UpdateSelectionResponse> bulkUpdateSelection(
            @PathVariable Long projectId,
            @RequestBody BulkUpdateSelectionRequest request
    ) {
        try {
            boolean selected = Boolean.TRUE.equals(request.getSelected());
            int updated = applicantSelectionService.bulkSetSelection(projectId, request.getApplicantIds(), selected);
            String msg = String.format("Updated %d applicants as %s", updated, selected ? "accepted" : "rejected");
            return ResponseEntity.ok(new UpdateSelectionResponse("OK", msg));
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UpdateSelectionResponse("ERROR", se.getMessage()));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateSelectionResponse("ERROR", iae.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UpdateSelectionResponse("ERROR", "Unexpected error"));
        }
    }
}
