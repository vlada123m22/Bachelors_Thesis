package com.example.timesaver.model.dto.applicants.selection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateSelectionResponse {
    private String status; // "OK"
    private String message; // e.g., "Applicant marked as accepted"
}