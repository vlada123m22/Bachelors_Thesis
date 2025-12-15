package com.example.timesaver.controller;

import com.example.timesaver.model.dto.application.*;
import com.example.timesaver.model.dto.application.*;
import com.example.timesaver.service.ApplicationService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/projects/apply")
public class ApplicationController {

    private final ApplicationService applicationService;


    private final ObjectMapper objectMapper;


    public ApplicationController(ApplicationService applicationService, ObjectMapper objectMapper) {
        this.applicationService = applicationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get form questions for a project (PUBLIC - no authentication required)
     * GET /projects/apply/{projectId}
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getForm(@PathVariable Long projectId) {
        try {
            GetFormResponse response = applicationService.getFormForProject(projectId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "Failure");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Submit application (PUBLIC - no authentication required)
     * POST /projects/apply
     *
     * Accepts multipart/form-data with:
     * - applicationData: JSON string containing SubmitApplicationRequest
     * - file_{questionNumber}: Files for FILE type questions
     *
     * IMPORTANT: File parameter names MUST match the question number!
     *
     * Example with multiple file questions:
     * If form has FILE questions at positions 3, 5, and 7:
     * - applicationData: {"projectId":1,"firstName":"John",...}
     * - file_3: resume.pdf (for question 3)
     * - file_5: portfolio.zip (for question 5)
     * - file_7: certificate.pdf (for question 7)
     *
     * The system automatically maps each file to its question by the number in the parameter name.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApplicationResponse> submitApplication(
            @RequestParam("applicationData") String applicationDataJson,
            @RequestParam(required = false) Map<String, MultipartFile> files) {

        try {
            // Parse JSON application data
            SubmitApplicationRequest request = objectMapper.readValue(
                    applicationDataJson,
                    SubmitApplicationRequest.class
            );
            // Extract file answers by question number
            Map<Integer, MultipartFile> fileAnswers = new HashMap<>();
            if (files != null && !files.isEmpty()) {
                for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                    String key = entry.getKey();
                    // Expected format: "file_1", "file_2", etc.
                    if (key.startsWith("file_")) {
                        try {
                            Integer questionNumber = Integer.parseInt(key.substring(5));
                            fileAnswers.put(questionNumber, entry.getValue());
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid file parameter name: " + key);
                        }
                    } else {
                        throw new RuntimeException("File parameter name must start with 'file_' ");
                    }
                }
            }

            // Submit application
            ApplicationResponse response = applicationService.submitApplication(request, fileAnswers);

            // Return appropriate status code
            if ("Success".equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else if ("TeamExists".equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            ApplicationResponse errorResponse = new ApplicationResponse(
                    "Failure",
                    "Invalid request format: " + e.getMessage()
            );
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


}