package com.netdisk.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransferControllerTest {

    @Test
    void analysis(){
        String fileName = "hhhhhaaaassssshhhhhhh_12344569.docx";
        String md5 = fileName.substring(0,fileName.lastIndexOf("_"));
        Long serialNo = Long.valueOf(fileName.substring(fileName.lastIndexOf("_")+1,fileName.lastIndexOf(".")));
        String contentType = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()-1);
        System.out.println(md5);
        System.out.println(serialNo);
        System.out.println(contentType);
    }

    @Test
    void createTask(){
        File file = new File("Users/star_wyx/Desktop/tmp/uid");
        if(!file.exists()){
            file.mkdirs();
        }

    }

}