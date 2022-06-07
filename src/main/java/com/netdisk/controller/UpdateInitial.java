package com.netdisk.controller;

import com.netdisk.config.FileProperties;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.module.User;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.Md5Service;
import com.netdisk.service.SharedService;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.SharedServiceImpl;
import com.netdisk.service.impl.UserServiceImpl;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Controller
public class UpdateInitial {


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

    @GetMapping("/updateSystem")
    @ResponseBody
    public Response update() {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        setUserIsShared();
        setShared();
        setMd5();
        setShareRootNode();
        setFolderSize();
        changeFileSystem();
        return assembly.success(null);
    }

    /**
     * fileNode: isShared
     * user: isShared
     */


    public void changeFileSystem() {
        log.info("===============changeFileSystem=================");
        List<User> userList = mongoTemplate.findAll(User.class, UserServiceImpl.USER_COLLECTION);
        for (User user : userList) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId()));
            List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            query.addCriteria(Criteria.where("nodeId").is(1));
            FileNode rootNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            for (FileNode fileNode : fileNodeList) {
                Query q = new Query();
                q.addCriteria(Criteria.where("userId").is(user.getUserId()));
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
        log.info("===============changeFileSystem END=================");
    }


    public void setMd5() {
        log.info("===============setMd5=================");
        List<User> userList = mongoTemplate.findAll(User.class, UserServiceImpl.USER_COLLECTION);
        for (User user : userList) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId()));
            List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            for (FileNode f : fileNodeList) {
                if (f.getMd5() != null) {
                    md5Service.increaseIndex(f.getMd5());
                }
            }
        }
        log.info("===============setMd5 END=================");
    }


    public void setShared() {
        log.info("===============setShared=================");
        Update update = new Update();
        update.set("isShared", false);
        mongoTemplate.updateMulti(new Query(), update, FileServiceImpl.FILE_COLLECTION);
        log.info("===============setShared END=================");
    }


    public void setFolderSize() {
        log.info("===============setFolderSize=================");
        List<User> userList = mongoTemplate.findAll(User.class, UserServiceImpl.USER_COLLECTION);
        for (User user : userList) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId()));
            query.addCriteria(Criteria.where("isFolder").is(true));
            List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            for (FileNode node : fileNodeList) {
                List<FileNode> children = nodeRepository.getSubTree(user.getUserId(), node.getNodeId(), null).get(0).getDescendants();
                long size = 0;
                for (FileNode child : children) {
                    if (!child.isFolder()) {
                        size += child.getFileSize();
                    }
                }
                Query q = new Query();
                q.addCriteria(Criteria.where("userId").is(user.getUserId()));
                q.addCriteria(Criteria.where("nodeId").is(node.getNodeId()));
                Update update = new Update();
                update.set("fileSize", size);
                mongoTemplate.updateFirst(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            }
        }
        log.info("===============setFolderSize END=================");
    }


    public void setUserIsShared() {
        log.info("===============setUserIsShared=================");
        Update update = new Update();
        update.set("isShared", false);
        mongoTemplate.updateMulti(new Query(), update, UserServiceImpl.USER_COLLECTION);
        log.info("===============setUserIsShared END=================");
    }


    public void setShareRootNode() {
        log.info("===============setShareRootNode=================");
        Share share = new Share(
                null,
                0L,
                "root",
                0L,
                "root",
                "/",
                "/",
                true,
                null,
                0L,
                "root",
                null,
                null,
                0L,
                ","

        );
        mongoTemplate.save(share, SharedServiceImpl.SHARED_COLLECTION);

        List<User> userList = mongoTemplate.findAll(User.class, UserServiceImpl.USER_COLLECTION);
        for (User user : userList) {
            sharedService.createUserRoot(user.getUserId(), user.getUserName());
        }
        log.info("===============setShareRootNode END=================");
    }

}
