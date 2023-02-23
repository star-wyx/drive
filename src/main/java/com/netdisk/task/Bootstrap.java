package com.netdisk.task;

import com.netdisk.config.FileProperties;
import com.netdisk.module.FileNode;
import com.netdisk.module.SeqInfo;
import com.netdisk.module.User;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomInfo;
import com.netdisk.service.impl.ChatServiceImpl;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.SeqServiceImpl;
import com.netdisk.service.impl.UserServiceImpl;
import com.netdisk.util.FfmpegUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// 每次项目启动时执行一次
@Component
public class Bootstrap implements ApplicationRunner {

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FfmpegUtil ffmpegUtil;

    @Autowired
    MongoTemplate mongoTemplate;

    @Value("${name}")
    String name;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        File root = new File(fileProperties.getRootDir());
        File profileDir = new File(fileProperties.getProfileDir());
        File mp4Dir = new File(fileProperties.getMp4Dir());
        File tmpPath = new File(fileProperties.getTmpPath());

        // 新建对应的目录
        if (!root.exists()) {
            root.mkdirs();
        }
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }
        if (!mp4Dir.exists()) {
            mp4Dir.mkdirs();
        }
        if (!tmpPath.exists()) {
            tmpPath.mkdirs();
        }

        ffmpegUtil.checkEncoders();
        //bootCheck();

        System.setProperty("thumbnailator.conserveMemoryWorkaround", "true");
        System.out.println("系统设置成功");

    }

    public void bootCheck() {
        System.out.println("start boot check");
        fileDocumentCheck();
        roomCheck();
        seqCheck();
        userCheck();
        System.out.println("finish boot check");
    }

    // 检查是否有根目录
    public void fileDocumentCheck() {
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(0));
        query.addCriteria(Criteria.where("parentId").is(0));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        if (fileNode == null) {
            fileNode = new FileNode(null,
                    0L,
                    0L,
                    "root",
                    "root",
                    null,
                    true,
                    null,
                    0L,
                    "root",
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            mongoTemplate.save(fileNode, FileServiceImpl.FILE_COLLECTION);
        }
    }

    // 检查是否有根聊天室
    public void roomCheck() {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(1));
        Room room = mongoTemplate.findOne(query, Room.class, ChatServiceImpl.ROOM_COLLECTION);
        if (room == null) {
            List<User> userList = mongoTemplate.findAll(User.class, UserServiceImpl.USER_COLLECTION);
            List<Long> userIdList = new ArrayList<>();
            for (User user : userList) {
                userIdList.add(user.getUserId());
            }

            room = new Room(
                    null,
                    1L,
                    "大咕咕澡堂",
                    "Main_Room_Image",
                    null,
                    userIdList,
                    null,
                    1L);

            mongoTemplate.save(room, ChatServiceImpl.ROOM_COLLECTION);

            for (Long i : userIdList) {
                RoomInfo roomInfo = new RoomInfo(null, room.getRoomId(), i, 0L, new Date().getTime(), 0L, false);
                mongoTemplate.save(roomInfo, ChatServiceImpl.ROOMINFO_COLLECTION);
            }
        }
    }

    // 检查各个用户是否有对应的seq
    public void seqCheck() {
        Query userQuery = new Query(Criteria.where("collName").is("user@id"));
        SeqInfo userInfo = mongoTemplate.findOne(userQuery, SeqInfo.class, SeqServiceImpl.SEQ_COLLECTION);
        if (userInfo == null) {
            userInfo = new SeqInfo(null, "user@id", 2L);
            mongoTemplate.save(userInfo, SeqServiceImpl.SEQ_COLLECTION);
        }

        Query roomQuery = new Query(Criteria.where("collName").is("room@id"));
        SeqInfo roomInfo = mongoTemplate.findOne(roomQuery, SeqInfo.class, SeqServiceImpl.SEQ_COLLECTION);
        if (roomInfo == null) {
            roomInfo = new SeqInfo(null, "room@id", 2L);
            mongoTemplate.save(roomInfo, SeqServiceImpl.SEQ_COLLECTION);
        }
    }

    // 检查根用户是否存在
    public void userCheck() {
        Query query = new Query();
        query.addCriteria(Criteria.where("userName").is("root"));
        User user = mongoTemplate.findOne(query, User.class, UserServiceImpl.USER_COLLECTION);
        if (user == null) {
            user = new User(null, "root", "root", "root",
                    null, null, null, false, null);
            mongoTemplate.save(user, UserServiceImpl.USER_COLLECTION);
        }
    }

}
