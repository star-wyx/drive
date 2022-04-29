package com.netdisk.service.impl;

import com.mongodb.client.MongoCollection;
import com.netdisk.module.Mp4;
import com.netdisk.service.Mp4Service;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@Slf4j
public class Mp4ServiceImpl implements Mp4Service {

    @Autowired
    MongoTemplate mongoTemplate;

    public static final String Mp4_COLLECTION = "mp4";


    @Override
    public Mp4 queryByMd5(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query, Mp4.class, Mp4_COLLECTION);
    }

    @Override
    public Mp4 queryByOtherMd5(String otherMd5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("otherMd5").is(otherMd5));
        return mongoTemplate.findOne(query, Mp4.class, Mp4_COLLECTION);
    }

    @Override
    public void add(String fileName, String md5, String storePath, String otherMd5, String status, Long userId) {
        Mp4 mp4 = new Mp4(null, fileName, md5, storePath, otherMd5, status, new Date(), userId);
        mongoTemplate.save(mp4);
    }

    @Override
    public void changeStatus(String otherMd5, String newStatus) {
        System.out.println("CHANGE STATUS");
        Query query = new Query();
        query.addCriteria(Criteria.where("otherMd5").is(otherMd5));
        Update update = new Update();
        update.set("status", newStatus);
        mongoTemplate.updateFirst(query, update, Mp4.class, Mp4_COLLECTION);
        System.out.println("CHANGE STATUS COMPLETED");
    }

    @Override
    public void setMd5(String otherMd5, String md5) {
        System.out.println("SET MD5");
        Query query = new Query();
        query.addCriteria(Criteria.where("otherMd5").is(otherMd5));
        Update update = new Update();
        update.set("md5",md5);
        mongoTemplate.updateFirst(query,update,Mp4.class,Mp4_COLLECTION);
        System.out.println("SET MD% COMPLETED");
    }

    @Override
    public void updateTime(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        Update update = new Update();
        update.set("uploadTime", new Date());
        mongoTemplate.findAndModify(query,update,Mp4.class,Mp4_COLLECTION);
    }

    @Override
    public boolean checkUserTask(Long userId, String otherMd5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("status").is("ING"));
        Mp4 mp4 = mongoTemplate.findOne(query,Mp4.class, Mp4_COLLECTION);
        if(mp4 == null){
            return true;
        }else if(mp4.getOtherMd5().equals(otherMd5)){
            return true;
        }else{
            return false;
        }
    }
}
