package com.netdisk.service.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ChatParamDTO;
import com.netdisk.module.DTO.MessageDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.User;
import com.netdisk.module.chat.*;
import com.netdisk.service.ChatService;
import com.netdisk.service.SeqService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    public static final String MESSAGE_COLLECTION = "message";

    public static final String ROOM_COLLECTION = "room";

    public static final String CHATINFO_COLLECTION = "chatInfo";

    @Autowired
    SeqService seqService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoClient mongoClient;

    @Override
    public void addRoom(List<Long> userList) {
        List<RoomUser> roomUsers = new ArrayList<>();
        Room room = new Room(
                null,
                seqService.getNextRoomId(),
                "main",
                "defaultImage",
                null,
                userList,
                null,
                1L);
        mongoTemplate.save(room, ROOM_COLLECTION);
    }

    @Override
    public Room getRoom(long roomId) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        return mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
    }

    @Override
    public MessageDTO addMessageToRoom(Long roomId, String content, byte[] files, Long replyMessageId, List<Long> usersTag, Long senderId) {
        Query userQuery = new Query(Criteria.where("userId").is(senderId));
        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
        String d = String.format("%tB", date) + " " + String.format("%te", date);
        String timeStamp = dateFormat.format(date);
        Message message = new Message(
                null,
                getNextMessageId(roomId),
                roomId,
                senderId,
                content,
                user.getUserName(),
                String.valueOf(user.getUserId()),
                d,
                timeStamp,
                replyMessageId,
                null,
                new Reactions(null, null));
        mongoTemplate.save(message, MESSAGE_COLLECTION);

        ChatInfo chatInfo = new ChatInfo(null, roomId, message.getMessageId(), senderId, false, true, false, false, false, false, false, false);
        mongoTemplate.save(chatInfo, CHATINFO_COLLECTION);

        Room room = getRoom(roomId);
        for (Long roomUserId : room.getUserList()) {
            if (roomUserId != senderId) {
                ChatInfo tmp = new ChatInfo(null, roomId, message.getMessageId(), roomUserId, false, true, false, false, false, false, false, false);
                mongoTemplate.save(tmp, CHATINFO_COLLECTION);
            }
        }

        MessageDTO res = new MessageDTO(message, chatInfo);
        return res;
    }

    @Override
    public long getNextMessageId(long roomId) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
        long res = room.getNextMessageId();
        Update update = new Update();
        update.set("nextMessageId", room.getNextMessageId() + 1);
        mongoTemplate.findAndModify(query, update, Room.class, ROOM_COLLECTION);
        return res;
    }

    @Override
    public List<RoomUser> getRoomUser(long roomId) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
        List<RoomUser> res = new ArrayList<>();
        for (Long i : room.getUserList()) {
            Query userQuery = new Query(Criteria.where("userId").is(i));
            User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
            String avatar = "http://192.168.1.143:9090/vavatar/" + user.getUserId() + "?time=" + "time";
            RoomUser roomUser = new RoomUser(user.getUserId(), user.getUserName(), avatar, user.getStatus());
            res.add(roomUser);
        }
        return res;
    }

    @Override
    public ParamDTO fetchmessages(long userId, long roomId, long startId) {
        ParamDTO paramDTO = new ParamDTO();
        List<MessageDTO> list = new ArrayList<>();
        paramDTO.setMessageEnd(false);

        if (startId == 0L) {
            startId = Integer.MAX_VALUE;
        }
        Query messageQuery = new Query(Criteria.where("roomId").is(roomId));
        messageQuery.addCriteria(Criteria.where("messageId").lt(startId));
        messageQuery.with(Sort.by(Sort.Direction.DESC, "messageId"));
        messageQuery.limit(20);
        List<Message> messages = mongoTemplate.find(messageQuery, Message.class, MESSAGE_COLLECTION);
        Collections.reverse(messages);

        Query chatInfoQuery = new Query(Criteria.where("roomId").is(roomId));
        chatInfoQuery.addCriteria(Criteria.where("userId").is(userId));
        chatInfoQuery.addCriteria(Criteria.where("messageId").lt(startId));
        chatInfoQuery.with(Sort.by(Sort.Direction.DESC, "messageId"));
        chatInfoQuery.limit(20);
        List<ChatInfo> chatInfos = mongoTemplate.find(chatInfoQuery, ChatInfo.class, CHATINFO_COLLECTION);
        Collections.reverse(chatInfos);

        for (int i = 0; i < messages.size(); i++) {
            MessageDTO tmp = new MessageDTO(messages.get(i), chatInfos.get(i));
            list.add(tmp);
        }

        if (list.size() == 0 || list.get(0).get_id() == 1L) {
            paramDTO.setMessageEnd(true);
        }

        paramDTO.setContent(list);

        return paramDTO;
    }

    @Override
    public void changeDistributed(long userId, long roomId, long messageId, boolean bool) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("roomId").is(roomId));
        query.addCriteria(Criteria.where("messageId").is(messageId));
        Update update = new Update();
        update.set("distributed", bool);
        mongoTemplate.findAndModify(query, update, ChatInfo.class, CHATINFO_COLLECTION);
    }

    @Override
    public ChatParamDTO iSeen(long roomId, long messageId, long userId) {
        ChatParamDTO chatParamDTO = null;
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        query.addCriteria(Criteria.where("messageId").is(messageId));
        Message message = mongoTemplate.findOne(query, Message.class, MESSAGE_COLLECTION);

        if (userId == message.getSenderId()) {
            return null;
        }

        query.addCriteria(Criteria.where("userId").is(message.getSenderId()));
        ChatInfo chatInfo = mongoTemplate.findOne(query, ChatInfo.class, CHATINFO_COLLECTION);
        if (!chatInfo.getSeen()) {
            chatParamDTO = changeSeenFrom(roomId, messageId, message.getSenderId());
            chatParamDTO.setUserId(message.getSenderId());
        }

        changeSeenFrom(roomId, messageId, userId);
        return chatParamDTO;
    }

    @Override
    public ChatParamDTO changeSeenFrom(long roomId, long messageId, long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        query.addCriteria(Criteria.where("messageId").lte(messageId));
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("seen").is(false));
        List<ChatInfo> list = mongoTemplate.find(query, ChatInfo.class, CHATINFO_COLLECTION);
        Update update = new Update();
        update.set("seen", true);
        mongoTemplate.updateMulti(query, update, ChatInfo.class, CHATINFO_COLLECTION);

        List<Long> seenList = new ArrayList<>();
        for (ChatInfo chatInfo : list) {
            seenList.add(chatInfo.getMessageId());
        }

        ChatParamDTO chatParamDTO = new ChatParamDTO();
        chatParamDTO.setRoomId(roomId);
        chatParamDTO.setSeenList(seenList);
        return chatParamDTO;
    }


}
