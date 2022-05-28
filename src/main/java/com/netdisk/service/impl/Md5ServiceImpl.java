package com.netdisk.service.impl;

import com.netdisk.module.Md5Record;
import com.netdisk.service.Md5Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class Md5ServiceImpl implements Md5Service {

    @Autowired
    MongoTemplate mongoTemplate;

    public static final String Md5_COLLECTION = "md5";

    @Override
    public long getIndex(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        Md5Record md5Record = mongoTemplate.findOne(query, Md5Record.class, Md5_COLLECTION);
        return md5Record.getIndex();
    }

    @Override
    public void deleteMd5(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        mongoTemplate.remove(query, Md5Record.class, Md5_COLLECTION);
    }

    @Override
    public void increaseIndex(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        Md5Record md5Record = mongoTemplate.findOne(query, Md5Record.class, Md5_COLLECTION);
        if (md5Record == null) {
            md5Record = new Md5Record(null, md5, 1);
            mongoTemplate.save(md5Record, Md5_COLLECTION);
        } else {
            Update update = new Update();
            update.set("index", md5Record.getIndex() + 1);
            mongoTemplate.findAndModify(query, update, Md5Record.class, Md5_COLLECTION);
        }
    }

    @Override
    public long decreaseIndex(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        Md5Record md5Record = mongoTemplate.findOne(query, Md5Record.class, Md5_COLLECTION);
        if (md5Record.getIndex() == 1) {
            deleteMd5(md5);
            return 0;
        } else {
            Update update = new Update();
            update.set("index", md5Record.getIndex() - 1);
            mongoTemplate.findAndModify(query, update, Md5Record.class, Md5_COLLECTION);
            return md5Record.getIndex() - 1;
        }
    }
}
