package com.netdisk.service.impl;

import com.mongodb.client.MongoCollection;
import com.netdisk.module.Mp4;
import com.netdisk.service.Mp4Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class Mp4ServiceImpl implements Mp4Service {

    @Autowired
    MongoTemplate mongoTemplate;

    public static final String CHUNK_COLLECTION = "mp4";


    @Override
    public Mp4 queryByMd5(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query,Mp4.class,CHUNK_COLLECTION);
    }

    @Override
    public Mp4 queryByOtherMd5(String otherMd5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("otherMd5").is(otherMd5));
        return mongoTemplate.findOne(query,Mp4.class,CHUNK_COLLECTION);
    }

    @Override
    public void add(String fileName, String md5, String storePath, String otherMd5) {
        Mp4 mp4 = new Mp4(null,fileName,md5,storePath,otherMd5);
        mongoTemplate.save(mp4);
    }
}
