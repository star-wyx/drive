package com.netdisk.module.DTO;

import com.netdisk.module.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.chat.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    @JsonProperty("user")
    private String user;
    @JsonProperty("user_pwd")
    private String userPwd;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("user_email")
    private String userEmail;
    @JsonProperty("message")
    private String message;
    @JsonProperty("user_id")
    private String userId;

    public User ToUser(Long userId) {
        Status status = new Status("offline", new Date().toString());
        return new User(userId, this.userName, this.userPwd, this.userEmail, null, null, null, false, status);
    }
}