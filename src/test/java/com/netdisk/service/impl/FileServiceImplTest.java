package com.netdisk.service.impl;

import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.UserService;
import com.netdisk.util.TypeComparator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceImplTest {

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Autowired
    TypeComparator typeComparator;

    @Test
    public void test() {
        User user = new User(null, "wyx", "123", "@qq.com");
//        fileService.createUserFile(user);
    }

    @Test
    public void queryFolderTest() {
        System.out.println(fileService.queryFileByNameId(2L, 1L, "c"));
    }

    @Test
    public void FileRank(){
        FileNode a = fileService.queryFolderById(2L,103L);
        FileNode b  = fileService.queryFolderById(2L,109L);
        FileDTO adto = new FileDTO(a);
        FileDTO bdto = new FileDTO(b);
        List<FileDTO> list = new ArrayList<>();
        list.add(adto);
        list.add(bdto);
        Collections.sort(list,typeComparator);
    }
}