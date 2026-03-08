package com.example.timesaver.model.dto.applicants.selection;

import lombok.Data;

@Data
public class UpdateSelectionRequest {
    private Boolean selected; // true = accepted, false = rejected
}