package com.netdisk.service.impl;

import com.netdisk.WebSocket.MessageEventHandler;
import com.netdisk.module.User;
import com.netdisk.module.chat.Message;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomInfo;
import com.netdisk.module.chat.Status;
import com.netdisk.service.ChatService;
import com.netdisk.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.support.QuerydslAbstractMongodbQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatServiceImplTest {

    @Autowired
    ChatService chatService;

    @Autowired
    UserService userService;

    @Autowired
    MongoTemplate mongoTemplate;

//    @Test
//    public void test() {
//        List<Long> userList = new ArrayList();
//        for (long i = 1; i <= 3; i++) {
//            userList.add(i);
//        }
//        chatService.addRoom(userList);
//    }
//
//    @Test
//    public void addUserStatus() {
//        Status status = new Status("offline", new Date().toString());
//        List<User> userList = mongoTemplate.findAll(User.class, UserServiceImpl.USER_COLLECTION);
//        for (User user : userList) {
//            user.setStatus(status);
//            mongoTemplate.save(user);
//        }
//    }

//    @Test
//    public void mongoTest() {
//        System.out.println(chatService.fetchmessages(3L, 1L, 10));
//    }
//
//
//    @Test
//    public void newRoom() {
//        List<Long> userList = new ArrayList<>();
//        userList.add(1L);
//        userList.add(2L);
//        userList.add(3L);
//        chatService.addRoom(userList, "main");
//    }
//
//    @Test
//    public void setLastMessageId() {
//        Date date = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
//        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
//        String d = dateFormat.format(date);
//        String timeStamp = timeFormat.format(date);
//        System.out.println(d);
//        System.out.println(timeStamp);
//    }

}