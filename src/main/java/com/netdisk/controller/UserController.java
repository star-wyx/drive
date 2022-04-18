package com.netdisk.controller;

import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.UserDTO;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


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
     * 用户登陆，成功则返回根目录
     *
     * @param userDTO user,用户名或邮箱
     *                user_pwd,密码
     * @return user_name, message
     */
    @PostMapping("/login")
    @ResponseBody
    public Response login(@RequestBody UserDTO userDTO) {
        Response res = userService.login(userDTO.getUser(), userDTO.getUserPwd());
        if (res.getCode() != 200) {
            return res;
        } else {
            return fileService.queryFolderRootContent((User) res.getData());
        }
    }

    /**
     * 用户注册
     *
     * @param userDTO user_name,用户名
     *                user_email,用户邮箱
     *                user_pwd,密码
     */
    @PostMapping("/signin")
    @ResponseBody
    public Response signin(@RequestBody UserDTO userDTO) {
        Response response = userService.add(userDTO);
        if (response.getCode() == 200) {
            fileService.createUserFile(userService.getUserByName(userDTO.getUserName()));
        }
        return response;
    }

    /**
     * 上传头像
     */
    @PostMapping("/updatePhoto")
    @ResponseBody
    public Response updatePhoto(@RequestParam("file") MultipartFile file,
                                @RequestParam("user_id") Long userId) {
        AssemblyResponse<Integer> assembly = new AssemblyResponse();
        if (userService.uploadPicture(file, userId) == 200) {
            return assembly.success(null);
        } else {
            return assembly.fail(400, null);
        }
    }

    /**
     * 修改密码
     */
    @GetMapping("/changePwd")
    @ResponseBody
    public Response changePwd(@RequestParam(value = "old_pwd") String oldPwd,
                              @RequestParam(value = "new_pwd") String newPwd,
                              @RequestParam(value = "user_id") Long userId) {
        int res = userService.updatePwd(userId, newPwd, oldPwd);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if (res == 200) {
            return assembly.success("password change successful");
        } else {
            return assembly.fail(res, "wrong password");
        }
    }
}
