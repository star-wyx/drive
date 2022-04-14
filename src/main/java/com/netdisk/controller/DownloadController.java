package com.netdisk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin(origins="http://192.168.1.169:9070", allowCredentials = "true")
public class DownloadController {

    @GetMapping(value = "vdownload/**")
    public void vdownload(HttpServletResponse response){
        response.setHeader("Content-Disposition", "attachment");
    }

}
