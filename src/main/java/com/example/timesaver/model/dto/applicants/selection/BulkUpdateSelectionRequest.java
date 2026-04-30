package com.example.timesaver.model.dto.applicants.selection;

import lombok.Data;
import java.util.List;

@Data
public class BulkUpdateSelectionRequest {
    private List<Integer> applicantIds;
    private Boolean selected; // set this status for all listed applicants
}