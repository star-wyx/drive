package com.netdisk.util;

import com.netdisk.config.FileProperties;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.FileService;
import com.netdisk.service.Md5Service;
import com.netdisk.service.SharedService;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.SharedServiceImpl;
import com.netdisk.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
class UpdateInitial {

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
    
    public void changeFileSystem() {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(2));
        List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        query.addCriteria(Criteria.where("nodeId").is(1));
        FileNode rootNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        for (FileNode fileNode : fileNodeList) {
            Query q = new Query();
            q.addCriteria(Criteria.where("userId").is(2));
            q.addCriteria(Criteria.where("nodeId").is(fileNode.getNodeId()));
            if (fileNode.isFolder()) {
                Update update = new Update();
                update.set("storePath", null);
                mongoTemplate.findAndModify(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            } else {
                String fileName = fileNode.getFileName();
                String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                File src = new File(fileProperties.getRootDir() + fileNode.getStorePath());
                File des = new File(fileProperties.getRootDir() + rootNode.getFilePath()
                        + File.separator + fileNode.getMd5() + "."
                        + suffix);
                if (des.exists()) {
                    try {
                        Files.move(src.toPath(), des.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Files.delete(src.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Update update = new Update();
                update.set("storePath", rootNode.getFilePath() + File.separator + fileNode.getMd5() + "." + suffix);
                mongoTemplate.findAndModify(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            }
        }
    }

    
    public void setMd5() {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(2));
        List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        for (FileNode f : fileNodeList) {
            if (f.getMd5() != null) {
                md5Service.increaseIndex(f.getMd5());
            }
        }
    }

    
    public void setShared() {
        Update update = new Update();
        update.set("isShared", false);
        mongoTemplate.updateMulti(new Query(), update, FileServiceImpl.FILE_COLLECTION);
    }

    
    public void setFolderSize() {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(2));
        query.addCriteria(Criteria.where("isFolder").is(true));
        List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        for (FileNode node : fileNodeList) {
            List<FileNode> children = nodeRepository.getSubTree(2L, node.getNodeId(), null).get(0).getDescendants();
            long size = 0;
            for (FileNode child : children) {
                if (child.getFileSize() != null) {
                    size += child.getFileSize();
                }
            }
            Query q = new Query();
            q.addCriteria(Criteria.where("userId").is(2));
            q.addCriteria(Criteria.where("nodeId").is(node.getNodeId()));
            Update update = new Update();
            update.set("fileSize", size);
            mongoTemplate.updateFirst(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        }
    }

    
    public void setUserIsShared(){
        Update update = new Update();
        update.set("isShared", false);
        mongoTemplate.updateMulti(new Query(), update, UserServiceImpl.USER_COLLECTION);
    }

    
    public void setShareRootNode(){
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(0));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        Share share = new Share(fileNode, 0L, "/all", "root");
        mongoTemplate.save(share, SharedServiceImpl.SHARED_COLLECTION);

        ////delete
        sharedService.createUserRoot(1L, "test");
        sharedService.createUserRoot(2L, "tom");
    }


    ////todo 重新算所有文件夹大小
}
