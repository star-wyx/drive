package com.netdisk.module.DTO;

import com.netdisk.module.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

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

    public User ToUser(Long userId){
        return new User(userId,this.userName,this.userPwd,this.userEmail);
    }
}