package com.netdisk.service.impl;

import com.netdisk.config.FileProperties;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.FileService;
import com.netdisk.service.Md5Service;
import com.netdisk.service.SharedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceImplTest {

    @Autowired
    FileService fileService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    Md5Service md5Service;

    @Autowired
    SharedService sharedService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    NodeRepository nodeRepository;

}