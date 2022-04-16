package com.netdisk.service.impl;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.UserService;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.TypeComparator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MyFileUtils myFileUtils;

    @Autowired
    FileProperties fileProperties;

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
    public void FileRank() {
        FileNode a = fileService.queryFolderById(2L, 103L);
        FileNode b = fileService.queryFolderById(2L, 109L);
        FileDTO adto = new FileDTO(a);
        FileDTO bdto = new FileDTO(b);
        List<FileDTO> list = new ArrayList<>();
        list.add(adto);
        list.add(bdto);
        Collections.sort(list, typeComparator);
    }

    @Test
    public void addStorePath() {
        for (int i = 1; i <= 2; i++) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(i));
            List<FileNode> list = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            for (FileNode fileNode : list) {
                Update update = new Update();
                update.set("storePath", fileNode.getFilePath());
                mongoTemplate.save(fileNode, FileServiceImpl.FILE_COLLECTION);
            }
        }
    }

    @Test
    public void base64() {
        String srcPath = "/tom/20211024_192750.jpg";
//        System.out.println(myFileUtils.encodeFileToBase64BinaryWithImageSize(srcPath, 1L));
        System.out.println(myFileUtils.commpressPicForScale(srcPath,
                fileProperties.getTmpPath() + "/1.jpg",
                50,
                0.7));
    }
}