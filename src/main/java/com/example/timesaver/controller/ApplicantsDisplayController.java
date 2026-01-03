package com.example.timesaver.controller;

import com.example.timesaver.model.dto.applicantsdisplay.GetParticipantsDTO;
import com.example.timesaver.model.dto.applicantsdisplay.GetTeamsDTO;
import com.example.timesaver.service.ApplicantsDisplayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{projectId}")
public class ApplicantsDisplayController {
    private final ApplicantsDisplayService applicantsDisplayService;

    public ApplicantsDisplayController(ApplicantsDisplayService applicantsDisplayService) {
        this.applicantsDisplayService = applicantsDisplayService;
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
}
