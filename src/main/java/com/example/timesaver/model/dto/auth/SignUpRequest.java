package com.example.timesaver.model.dto.auth;

import lombok.Data;

@Data
public class SignUpRequest {
    private String UserName;
    private String Password;
    private String Email;
    private String CreationDateTime; // ignored, server sets real timestamp
}
