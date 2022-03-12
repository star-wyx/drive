package com.disk.controller;

import com.disk.entity.User;
import com.disk.service.UserService;
import com.disk.util.AssemblyResponse;
import com.disk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@CrossOrigin(origins="*")
@RequestMapping("/user")
public class UserHandler {
    @Autowired
    private UserService userService;

    @GetMapping("/findAll")
    public ModelAndView findAll(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("list",userService.findAll());
        return modelAndView;
    }

    @PostMapping("/queryUser")
    @ResponseBody
    public User queryUser(@RequestBody Map<String,Object> map){
        return userService.queryUser(map);
    }

    @PostMapping("/login")
    @ResponseBody
    public Response login(@RequestBody Map<String,Object> map){
        User user = userService.queryUser(map);
        AssemblyResponse<User> assembly = new AssemblyResponse<>();
        if(user==null){
            return assembly.fail(201,null);
        }else{
            return assembly.success(user);
        }
    }
}
