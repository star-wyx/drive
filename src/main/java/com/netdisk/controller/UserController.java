package com.netdisk.controller;

import com.netdisk.pojo.DTO.UserDTO;
import com.netdisk.pojo.User;
import com.netdisk.service.DirService;
import com.netdisk.service.UserService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@CrossOrigin(origins="http://192.168.1.169:9070", allowCredentials = "true")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private DirService dirService;

    @PostMapping("/login")
    @ResponseBody
    public Response login(@RequestBody Map<String,Object> map){
        User user;
        String u = (String) map.get("user");
        AssemblyResponse<String> assemblyFail = new AssemblyResponse<>();
        if(u.contains("@")){
            map.put("user_email",u);
            user = userService.queryUserEmailPwd(map);
            if(user == null){
                if(userService.queryUserByEmail(map)==null){
                    return assemblyFail.fail(403,"登陆邮箱不存在");
                }else{
                    return assemblyFail.fail(401,"登陆密码错误");
                }
            }
        }else{
            map.put("user_name",u);
            user = userService.queryUserNamePwd(map);
            if(user == null){
                if(userService.queryUserByName(map)==null){
                    return assemblyFail.fail(402,"登陆用户名不存在");
                }else{
                    return assemblyFail.fail(401,"登陆密码错误");
                }
            }
        }
        AssemblyResponse<UserDTO> assemblySuccess = new AssemblyResponse<>();
        return assemblySuccess.success(new UserDTO(user,"login successfully!"));
    }


    @PostMapping("/signin")
    @ResponseBody
    public Response signin(@RequestBody Map<String, Object> map) throws IOException {
        User byName = userService.queryUserByName(map);
        User byEmail = userService.queryUserByEmail(map);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if(byName==null && byEmail==null){
            int row = userService.insertUser(map);
            if(row!=0){
                User user = userService.queryUserByEmail(map);
                dirService.NewFolderUser(user);
                return assembly.success("注册成功");
            }else{
                return assembly.fail(100,"注册失败");
            }
        }else if(byName != null){
            return assembly.fail(404,"用户名被占用");
        }else {
            return assembly.fail(405,"邮箱被占用");
        }
    }

    @PostMapping("/data")
    @ResponseBody
    public Response queryData(@RequestBody Map<String,Object> map){
        User byName = userService.queryUserByName(map);
        AssemblyResponse<List<String>> assembly = new AssemblyResponse<>();
        if(byName==null){
            return assembly.fail(204,null);
        }
        List<String> res = userService.queryDataByUserId(byName.getUserId());
        return assembly.success(res);
    }
}
