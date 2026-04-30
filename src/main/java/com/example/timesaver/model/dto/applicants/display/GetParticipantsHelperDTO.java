package com.example.timesaver.model.dto.applicants.display;

//a DTO that includes participant/applicant ids for applicants page to avoid unnecessary fields in Applicant class

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetParticipantsHelperDTO {
    private Integer applicantId;
    private String firstName;
    private String lastName;
    private String teamName;
    private Boolean isSelected;

    public GetParticipantsHelperDTO(Integer applicantId, String firstName, String lastName, String teamName, Boolean isSelected) {
        this.applicantId = applicantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.teamName = teamName;
        this.isSelected = isSelected;
    }
}
