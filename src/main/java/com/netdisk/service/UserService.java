package com.netdisk.service;

import com.netdisk.module.DTO.UserDTO;
import com.netdisk.module.User;
import com.netdisk.util.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {

    Response login(UserDTO userDTO);

    /**
     * 添加用户
     * userName, userEmail, userPwd
     */
    Response add(UserDTO userDTO);

    /**
     * 根据用户名查询用户
     * user或userName
     */
    User getUserByName(String userName);

    /**
     * 根据邮箱查询用户
     * user或userEmail
     */
    User getUserByEmail(String userEmail);
}
