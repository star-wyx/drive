package com.disk.entity;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String userName;
    private String userPwd;
    private String userEmail;
}
