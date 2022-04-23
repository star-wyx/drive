package com.netdisk.service.impl;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Mp4;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.Mp4Service;
import com.netdisk.service.UserService;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.TypeComparator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceImplTest {

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Autowired
    TypeComparator typeComparator;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MyFileUtils myFileUtils;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    Mp4Service mp4Service;

    @Test
    public void test() {
//        User user = new User(null, "wyx", "123", "@qq.com",null);
//        fileService.createUserFile(user);
    }

    @Test
    public void queryFolderTest() {
        System.out.println(fileService.queryFileByNameId(2L, 1L, "c"));
    }

    @Test
    public void FileRank() {
        FileNode a = fileService.queryFolderById(2L, 103L);
        FileNode b = fileService.queryFolderById(2L, 109L);
        FileDTO adto = new FileDTO(a);
        FileDTO bdto = new FileDTO(b);
        List<FileDTO> list = new ArrayList<>();
        list.add(adto);
        list.add(bdto);
        Collections.sort(list, typeComparator);
    }

    @Test
    public void addStorePath() {
        for (int i = 1; i <= 2; i++) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(i));
            List<FileNode> list = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            for (FileNode fileNode : list) {
                Update update = new Update();
                update.set("storePath", fileNode.getFilePath());
                mongoTemplate.save(fileNode, FileServiceImpl.FILE_COLLECTION);
            }
        }
    }

    @Test
    public void base64() {
        Long userId = 2L;
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        List<FileNode> list = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        for (FileNode fileNode : list) {
            if (fileNode.getContentType().equals(fileProperties.getIcon().get("picture"))) {
                String srcPath = fileProperties.getRootDir() + fileNode.getStorePath();
                String desPath = fileProperties.getTmpPath() + "/tmp.jpg";
                query = new Query();
                query.addCriteria(Criteria.where("userId").is(userId));
                query.addCriteria(Criteria.where("nodeId").is(fileNode.getNodeId()));
                Update update = new Update();
                String base64 = myFileUtils.commpressPicForScale(srcPath,
                        desPath,
                        50,
                        0.7);
                update.set("base64", base64);
                mongoTemplate.findAndModify(query, update, FileNode.class);
            }
        }
    }

    @Test
    public void sortFileNode() {
        User user = userService.getUserById(2L);
        List<List> lists = fileService.queryFolderContent(user, 1L);
        List<FileNode> files = lists.get(1);
        files.sort(typeComparator);
    }

    @Test
    public void mp4Test() {
        Mp4 mp4 = mp4Service.queryByMd5("66422e08315447d69943959ac8ded578");
        System.out.println(mp4);
    }

    @Test
    public void updateFileSize() {
        Long userId = 1L;
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        long size = 0L;
        List<FileNode> list = fileService.queryAllFiles(userId);
        for (FileNode fileNode : list) {
            if (!fileNode.isFolder()) {
                Query q = new Query();
                q.addCriteria(Criteria.where("userId").is(userId));
                q.addCriteria(Criteria.where("nodeId").is(fileNode.getNodeId()));
                File file = new File(fileProperties.getRootDir() + fileNode.getStorePath());
                Update update = new Update();
                update.set("fileSize", file.length());
                size += file.length();
                mongoTemplate.findAndModify(q, update, FileNode.class);
            }
        }

        Query userQuery = new Query();
        userQuery.addCriteria(Criteria.where("userId").is(userId));
        Update update = new Update();
        update.set("usedSize", size / 8);
        update.set("totalSize", fileProperties.getDefaultSpace());
        mongoTemplate.findAndModify(userQuery, update, User.class, UserServiceImpl.USER_COLLECTION);
    }

    @Test
    public void testDate() {
        Long userId = 1L;
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        List<FileNode> list = mongoTemplate.find(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        for (FileNode f : list) {
            Query q = new Query();
            q.addCriteria(Criteria.where("userId").is(userId));
            q.addCriteria(Criteria.where("nodeId").is(f.getNodeId()));
            Update update = new Update();
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            update.set("uploadTime", formatter.format(date));
            mongoTemplate.findAndModify(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        }
    }

    @Test
    public void folderSize() {
        File file = new File(fileProperties.getRootDir() + "tom");
        System.out.println(file.length());
    }

    @Test
    public void percentage() {
        long userId = 2L;
        long filmSize = 0L;
        long musicSize = 0L;
        long pictureSize = 0L;
        long others = 0L;
        long remain = 0L;
        User user = userService.getUserById(userId);
        List<FileNode> list = fileService.queryAllFiles(userId);
        for (FileNode fileNode : list) {
            if (!fileNode.isFolder()) {
                if (fileNode.getContentType().equals(fileProperties.getIcon().get("film"))) {
                    filmSize += fileNode.getFileSize();
                } else if (fileNode.getContentType().equals(fileProperties.getIcon().get("music"))) {
                    musicSize += fileNode.getFileSize();
                } else if (fileNode.getContentType().equals(fileProperties.getIcon().get("picture"))) {
                    pictureSize += fileNode.getFileSize();
                } else {
                    others += fileNode.getFileSize();
                }
            }
        }
        remain = userService.availableSpace(user.getUserId());
        List<Long> longs = new ArrayList<>();
        longs.add(filmSize);
        longs.add(musicSize);
        longs.add(pictureSize);
        longs.add(others);
        longs.add(remain);

        System.out.println(myFileUtils.getPercentValue(longs, 2));

    }
}