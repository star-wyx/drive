package com.disk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {
    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("user_pwd")
    private String userPwd;
    @JsonProperty("user_email")
    private String userEmail;
}
