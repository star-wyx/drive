package com.netdisk.task;

import com.netdisk.config.FileProperties;
import com.netdisk.module.Chunk;
import com.netdisk.module.Mp4;
import com.netdisk.module.UploadRecord;
import com.netdisk.service.ChunkService;
import com.netdisk.service.impl.ChunkServiceImpl;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.Mp4ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author riemann
 * @date 2019/06/23 18:15
 */
@Component
@Slf4j
public class DeleteTmp extends QuartzJobBean {

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FileProperties fileProperties;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        deleteTmp();

        deleteHistory();

        deleteMp4();

    }

    private void deleteTmp(){
        Query query = new Query();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
//        calendar.add(Calendar.MINUTE, -1);
        query.addCriteria(Criteria.where("uploadTime").lte(calendar.getTime()));
        List<Chunk> chunkList = mongoTemplate.find(query, Chunk.class, ChunkServiceImpl.CHUNK_COLLECTION);
        mongoTemplate.remove(query,Chunk.class,ChunkServiceImpl.CHUNK_COLLECTION);
        for(Chunk chunk: chunkList){
            File file = new File(chunk.getStorePath());
            file.delete();
        }
        System.out.println("auto delete tmp file");
    }

    private void deleteHistory(){
        Query query = new Query();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -10);
//        calendar.add(Calendar.MINUTE, -1);
        query.addCriteria(Criteria.where("recordDate").lte(calendar.getTime()));
        mongoTemplate.remove(query, UploadRecord.class,ChunkServiceImpl.UPLOADRECORD_COLLECTION);
        System.out.println("auto delete history");

    }

    public void deleteMp4(){
        Query query = new Query();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
//        calendar.add(Calendar.SECOND, -25);
        calendar.add(Calendar.DATE, -1);
        query.addCriteria(Criteria.where("uploadTime").lte(calendar.getTime()));
        List<Mp4> list = mongoTemplate.find(query,Mp4.class, Mp4ServiceImpl.Mp4_COLLECTION);
        mongoTemplate.remove(query, Mp4.class, Mp4ServiceImpl.Mp4_COLLECTION);
        for(Mp4 mp4 : list){
            File file = new File(mp4.getStorePath());
            file.delete();
        }
        System.out.println("auto delete mp4");
    }
}
