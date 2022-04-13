package com.netdisk.service.impl;

import ch.qos.logback.core.util.FileUtil;
import com.netdisk.config.FileProperties;
import com.netdisk.module.Chunk;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.service.ChunkService;
import com.netdisk.service.FileService;
import com.netdisk.service.UserService;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Service
public class ChunkServiceImpl implements ChunkService {

    public static final String CHUNK_COLLECTION = "chunk";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Override
    public Long createTask(String md5, String uuid, Long userId, Long nodeId, String fileName) {
        Long res;
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        if (chunk == null) {
            String tmpPath = fileProperties.getTmpPath() + File.separator + uuid;
            File tmpFolder = new File(tmpPath);
            if (!tmpFolder.exists()) {
                tmpFolder.mkdirs();
            }
            chunk = new Chunk(null, 0L, uuid, md5, userId, nodeId, fileName, tmpPath);
            mongoTemplate.save(chunk, CHUNK_COLLECTION);
            res = -1L;
        } else {
            res = chunk.getSerialNo();
        }
        return res;
    }

    @Override
    public int uploadSlice(MultipartFile file, String uuid, String sliceName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        Long serialNo = Long.valueOf(sliceName.substring(sliceName.lastIndexOf("_") + 1, sliceName.lastIndexOf(".")));
        if (serialNo <= chunk.getSerialNo()) {
            return 200;
        } else if (serialNo == chunk.getSerialNo() + 1) {
            File newFile = new File(chunk.getStorePath(), serialNo + ".tmp");
            try {
                file.transferTo(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Update update = new Update();
            update.set("serialNo", chunk.getSerialNo() + 1);
            mongoTemplate.updateFirst(query, update, Chunk.class, CHUNK_COLLECTION);
            return 200;
        } else {
            return 454;
        }
    }

    @Override
    public int merge(String uuid, String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        assert chunk != null;
        FileNode folder = fileService.queryFolderById(chunk.getUserId(), chunk.getNodeId());
        User user = userService.getUserById(chunk.getUserId());
        String availableFileName = fileService.availableFileName(user,chunk.getNodeId(),chunk.getFileName());
        File newFile = new File(fileProperties.getRootDir() + folder.getFilePath(), availableFileName);
        byte[] byt = new byte[fileProperties.getSliceSizeMB() * 1024 * 1024];
        try {
            newFile.createNewFile();
            FileInputStream in = null;
            FileOutputStream out = new FileOutputStream(newFile, true);
            for (int i = 1; i <= chunk.getSerialNo(); i++) {
                int len;
                in = new FileInputStream(new File(chunk.getStorePath(), i + ".tmp"));
                while((len = in.read(byt)) != -1){
//                    System.out.println("------" + len);
                    out.write(byt, 0 , len);
                }
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return 500;
        }

        try {
            FileUtils.deleteDirectory(new File(chunk.getStorePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileService.insertFileNode(user,chunk.getNodeId(),availableFileName);
        return 200;
    }

    @Override
    public Long deleteChunk(String uuid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        return mongoTemplate.remove(query,Chunk.class, CHUNK_COLLECTION).getDeletedCount();
    }

    @Override
    public int abort(String uuid, String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query,Chunk.class,CHUNK_COLLECTION);
        if(chunk == null){
            return 453;
        }
        boolean bol = FileUtils.deleteQuietly(new File(chunk.getStorePath()));
        mongoTemplate.remove(chunk);
        return 200;
    }
}
