package com.netdisk.controller;

import com.netdisk.module.Chunk;
import com.netdisk.module.UploadRecord;
import com.netdisk.service.ChunkService;
import com.netdisk.service.impl.ChunkServiceImpl;
import com.netdisk.util.AssemblyResponse;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransferControllerTest {

    @Autowired
    ChunkService chunkService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void analysis() {
        String fileName = "hhhhhaaaassssshhhhhhh_12344569.docx";
        String md5 = fileName.substring(0, fileName.lastIndexOf("_"));
        Long serialNo = Long.valueOf(fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf(".")));
        String contentType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length() - 1);
        System.out.println(md5);
        System.out.println(serialNo);
        System.out.println(contentType);
    }

    @Test
    void createTask() {
        File file = new File("Users/star_wyx/Desktop/tmp/uid");
        if (!file.exists()) {
            file.mkdirs();
        }

    }


    @Test
    public void uploadHistory() {
//        UploadRecord uploadRecord = new UploadRecord(2L, "test", "test", "test", "test", 1, "what", "what", true, true, true, true);
//        UploadRecord uploadRecord1 = new UploadRecord(2L, "test2", "test2", "test2", "test2", 1, "what", "what", true, true, true, true);
//        mongoTemplate.save(uploadRecord, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
//        mongoTemplate.save(uploadRecord1, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
//
//        Query query = new Query();
//        query.addCriteria(Criteria.where("userId").is(2));
//        List<UploadRecord> list = mongoTemplate.find(query, UploadRecord.class, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
//        AssemblyResponse<List> assembly = new AssemblyResponse();
//        System.out.println(assembly.success(list));
    }

}