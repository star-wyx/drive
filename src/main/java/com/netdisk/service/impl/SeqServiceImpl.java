package com.netdisk.service.impl;

import com.netdisk.module.SeqInfo;
import com.netdisk.service.SeqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SeqServiceImpl implements SeqService {

    public static final String SEQ_COLLECTION = "sequence";

    @Autowired
    MongoTemplate mongoTemplate;

    // 生成一个有效的seq id
    @Override
    public long getNextSeqId(String collName) {
        Query query = new Query(Criteria.where("collName").is(collName));
        Update update = new Update();
        update.inc("seqId",1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        SeqInfo seqInfo = mongoTemplate.findAndModify(query,update,options,SeqInfo.class);
        assert seqInfo != null;
        return seqInfo.getSeqId();
    }

    @Override
    public void insertUser(String userName) {
        SeqInfo seqInfo = new SeqInfo(null,userName,0L);
        mongoTemplate.save(seqInfo,SEQ_COLLECTION);
    }

    // 生成一个新的有效的user id
    @Override
    public long getNextUserId() {
        Query query = new Query(Criteria.where("collName").is("user@id"));
        SeqInfo seqInfo = mongoTemplate.findOne(query,SeqInfo.class,SEQ_COLLECTION);
        if(seqInfo == null){
            seqInfo = new SeqInfo(null,"user@id",2L);
            mongoTemplate.save(seqInfo,SEQ_COLLECTION);
            return 1L;
        }
        Update update = new Update();
        update.set("seqId",seqInfo.getSeqId()+1L);
        mongoTemplate.findAndModify(query,update,SeqInfo.class,SEQ_COLLECTION);
        return seqInfo.getSeqId();
    }

    // 生成一个新的room id
    @Override
    public long getNextRoomId() {
        Query query = new Query(Criteria.where("collName").is("room@id"));
        SeqInfo seqInfo = mongoTemplate.findOne(query, SeqInfo.class, SEQ_COLLECTION);
        if(seqInfo == null){
            seqInfo = new SeqInfo(null, "room@id", 2L);
            mongoTemplate.save(seqInfo, SEQ_COLLECTION);
            return 1L;
        }
        Update update = new Update();
        update.set("seqId",seqInfo.getSeqId()+1L);
        mongoTemplate.findAndModify(query,update,SeqInfo.class,SEQ_COLLECTION);
        return seqInfo.getSeqId();
    }
}
