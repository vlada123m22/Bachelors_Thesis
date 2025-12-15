package com.example.timesaver.model.dto.auth;

public class SignUpRequest {
    private String UserName;
    private String Password;
    private String CreationDateTime; // ignored, server sets real timestamp

    public String getUserName() { return UserName; }
    public void setUserName(String userName) { UserName = userName; }

    public String getPassword() { return Password; }
    public void setPassword(String password) { Password = password; }

    public String getCreationDateTime() { return CreationDateTime; }
    public void setCreationDateTime(String creationDateTime) { CreationDateTime = creationDateTime; }
}
