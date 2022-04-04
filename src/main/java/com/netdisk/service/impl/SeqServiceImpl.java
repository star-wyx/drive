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
}
