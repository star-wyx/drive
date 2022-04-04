package com.netdisk.controller;

import com.netdisk.module.DTO.UserDTO;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.util.Response;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Api(value = "用户接口")
@Controller
@RequestMapping("/user")
@CrossOrigin(origins = "http://192.168.1.169:9070", allowCredentials = "true")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SeqService seqService;

    /**
     * 用户登陆
     * @param userDTO
     * user,用户名或邮箱
     * user_pwd,密码
     * @return
     * user_name, message
     */
    @PostMapping("/login")
    @ResponseBody
    public Response login(@RequestBody UserDTO userDTO) {
        return userService.login(userDTO);
    }

    /**
     * 用户注册
     * @param userDTO
     * user_name,用户名
     * user_email,用户邮箱
     * user_pwd,密码
     */
    @PostMapping("/signin")
    @ResponseBody
    public Response signin(@RequestBody UserDTO userDTO){
        Response response = userService.add(userDTO);
        if(response.getCode()==200){
            fileService.createUserFile(userService.getUserByName(userDTO.getUserName()));
        }
        return response;
    }
}
