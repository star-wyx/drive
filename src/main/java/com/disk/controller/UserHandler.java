package com.disk.controller;

import com.disk.entity.User;
import com.disk.service.UserService;
import com.disk.util.AssemblyResponse;
import com.disk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins="http://192.168.1.169:9070", allowCredentials = "true")
@RequestMapping("/user")
public class UserHandler {
    @Autowired
    private UserService userService;

//    @Deprecated
//    @GetMapping("/findAll")
//    public ModelAndView findAll(){
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("index");
//        modelAndView.addObject("list",userService.findAll());
//        return modelAndView;
//    }
//
//    @PostMapping("/queryUser")
//    @ResponseBody
//    public User queryUser(@RequestBody Map<String,Object> map){
//        return userService.queryUser(map);
//    }

    @PostMapping("/login")
    @ResponseBody
    public Response login(@RequestBody Map<String,Object> map){
        User user;
        String u = (String) map.get("user");
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if(u.contains("@")){
            map.put("user_email",u);
            user = userService.queryUserEmailPwd(map);
            if(user == null){
                if(userService.queryUserByEmail(map)==null){
                    return assembly.fail(403,"登陆邮箱不存在");
                }else{
                    return assembly.fail(401,"登陆密码错误");
                }
            }
        }else{
            map.put("user_name",u);
            user = userService.queryUserNamePwd(map);
            if(user == null){
                if(userService.queryUserByName(map)==null){
                    return assembly.fail(402,"登陆用户名不存在");
                }else{
                    return assembly.fail(401,"登陆密码错误");
                }
            }
        }
        return assembly.success("login successful");
    }


    @PostMapping("/signin")
    @ResponseBody
    public Response signin(@RequestBody Map<String, Object> map){
        User byName = userService.queryUserByName(map);
        User byEmail = userService.queryUserByEmail(map);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if(byName==null && byEmail==null){
            int row = userService.insertUser(map);
            if(row!=0){
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
