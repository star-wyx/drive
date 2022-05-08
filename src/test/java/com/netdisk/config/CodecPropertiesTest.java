package com.netdisk.config;

import com.netdisk.module.FileNode;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.task.Bootstrap;
import com.netdisk.util.MyFileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodecPropertiesTest {

    @Autowired
    CodecProperties codecProperties;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    MyFileUtils myFileUtils;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    public void codeTest(){

        codecProperties.getEncoder().put("h264_qsv",true);
        System.out.println(codecProperties.getEncoder());
        Map<String, Boolean> encoder = codecProperties.getEncoder();
        Map<String, Boolean> decoder = codecProperties.getDecoder();
        System.out.println(encoder);
        System.out.println(decoder);
    }

}