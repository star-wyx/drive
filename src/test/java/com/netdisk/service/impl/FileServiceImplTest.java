package com.netdisk.service.impl;

import com.netdisk.module.User;
import com.netdisk.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceImplTest {

    @Autowired
    FileService fileService;

    @Test
    public void test(){
        User user = new User(null,"wyx","123","@qq.com");
        fileService.createUserFile(user);
    }

}