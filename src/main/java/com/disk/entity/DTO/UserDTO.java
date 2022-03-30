package com.disk.entity.DTO;

import com.disk.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDTO {
    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("user_pwd")
    private String userPwd;
    @JsonProperty("user_email")
    private String userEmail;

    public UserDTO(User user){
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userPwd = user.getUserPwd();
        this.userEmail = user.getUserEmail();
    }
}