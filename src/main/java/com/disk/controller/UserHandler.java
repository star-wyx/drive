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
@CrossOrigin(origins="*")
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
        User user = userService.queryUserNamePwd(map);
        AssemblyResponse<User> assembly = new AssemblyResponse<>();
        if(user==null){
            return assembly.fail(201,null);
        }else{
            return assembly.success(user);
        }
    }

//    @ResponseBody
//    public Response signin0(@RequestBody Map<String,Object> map){
//        int row = userService.signIn(map);
//        AssemblyResponse<Integer> assembly = new AssemblyResponse<Integer>();
//        if(row!=0){
//            return assembly.success(row);
//        }else{
//            return assembly.fail(100,row);
//        }
//    }

    @PostMapping("/signin")
    @ResponseBody
    public Response signin(@RequestBody Map<String, Object> map){
        User byName = userService.queryUserByName(map);
        User byEmail = userService.queryUserByEmail(map);
        AssemblyResponse<Integer> assembly = new AssemblyResponse<>();
        if(byName==null && byEmail==null){
            int row = userService.insertUser(map);
            if(row!=0){
                return assembly.success(row);
            }else{
                return assembly.fail(100,row);
            }
        }else if(byName != null){
            return assembly.fail(202,null);
        }else {
            return assembly.fail(203,null);
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
