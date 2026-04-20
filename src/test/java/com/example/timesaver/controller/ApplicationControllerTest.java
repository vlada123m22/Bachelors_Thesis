package com.example.timesaver.controller;

import com.example.timesaver.model.dto.application.ApplicationResponse;
import com.example.timesaver.model.dto.application.GetFormResponse;
import com.example.timesaver.model.dto.application.SubmitApplicationRequest;
import com.example.timesaver.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ApplicationController applicationController;

    @Test
    public void testGetFormSuccess() {
        GetFormResponse resp = new GetFormResponse(Collections.emptyList());
        when(applicationService.getFormForProject(1L)).thenReturn(resp);

        ResponseEntity<?> response = applicationController.getForm(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testGetFormNotFound() {
        when(applicationService.getFormForProject(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = applicationController.getForm(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSubmitApplicationSuccess() throws Exception {
        String json = "{}";
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        ApplicationResponse appResp = new ApplicationResponse("Success", "Ok");
        
        when(objectMapper.readValue(json, SubmitApplicationRequest.class)).thenReturn(req);
        when(applicationService.submitApplication(any(), any())).thenReturn(appResp);

        ResponseEntity<ApplicationResponse> response = applicationController.submitApplication(json, Collections.emptyMap());
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Success", response.getBody().getStatus());
    }

    @Test
    public void testSubmitApplicationWithFiles() throws Exception {
        String json = "{}";
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        ApplicationResponse appResp = new ApplicationResponse("Success", "Ok");
        
        Map<String, MultipartFile> files = new HashMap<>();
        files.put("file_1", null);

        when(objectMapper.readValue(json, SubmitApplicationRequest.class)).thenReturn(req);
        when(applicationService.submitApplication(any(), any())).thenReturn(appResp);

        ResponseEntity<ApplicationResponse> response = applicationController.submitApplication(json, files);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testSubmitApplicationInvalidFileName() throws Exception {
        String json = "{}";
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        when(objectMapper.readValue(json, SubmitApplicationRequest.class)).thenReturn(req);

        Map<String, MultipartFile> files = new HashMap<>();
        files.put("invalid_name", null);

        ResponseEntity<ApplicationResponse> response = applicationController.submitApplication(json, files);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testSubmitApplicationConflict() throws Exception {
        String json = "{}";
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        ApplicationResponse appResp = new ApplicationResponse("Failure", "Exists", true);
        
        when(objectMapper.readValue(json, SubmitApplicationRequest.class)).thenReturn(req);
        when(applicationService.submitApplication(any(), any())).thenReturn(appResp);

        ResponseEntity<ApplicationResponse> response = applicationController.submitApplication(json, null);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
