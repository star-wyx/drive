package com.netdisk.service.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ChatParamDTO;
import com.netdisk.module.DTO.MessageDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.RoomDTO;
import com.netdisk.module.User;
import com.netdisk.module.chat.*;
import com.netdisk.service.ChatService;
import com.netdisk.service.SeqService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    public static final String MESSAGE_COLLECTION = "message";

    public static final String ROOM_COLLECTION = "room";

    public static final String CHATINFO_COLLECTION = "chatInfo";

    public static final String ROOMINFO_COLLECTION = "roomInfo";

    @Autowired
    SeqService seqService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoClient mongoClient;

    @Override
    public Room addRoom(List<Long> userList, String roomName) {
        Room room = new Room(
                null,
                seqService.getNextRoomId(),
                roomName,
                "defaultImage",
                null,
                userList,
                null,
                1L);
        mongoTemplate.save(room, ROOM_COLLECTION);

        for (Long i : userList) {
            RoomInfo roomInfo = new RoomInfo(null, room.getRoomId(), i, 0L, new Date().getTime(), 0L);
            mongoTemplate.save(roomInfo, ROOMINFO_COLLECTION);
        }
        return room;
    }

    @Override
    public Room getRoom(long roomId) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        return mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
    }

    @Override
    public List<RoomDTO> getAllRoom(long userId) {
        Query roomInfoQuery = new Query();
        roomInfoQuery.addCriteria(Criteria.where("userId").is(userId));
        List<RoomInfo> roomInfos = mongoTemplate.find(roomInfoQuery, RoomInfo.class, ROOMINFO_COLLECTION);

        List<RoomDTO> res = new ArrayList<>();
        for (RoomInfo roomInfo : roomInfos) {
            Query query = new Query();
            query.addCriteria(Criteria.where("roomId").is(roomInfo.getRoomId()));
            Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
            RoomDTO tmp = roomToDTO(room, roomInfo, userId, roomInfo.getUnread());
            res.add(tmp);
        }
        return res;
    }

    @Override
    public MessageDTO addMessageToRoom(Long roomId, String content, byte[] files, Long replyMessageId, List<Long> usersTag, Long senderId) {
        Query userQuery = new Query(Criteria.where("userId").is(senderId));
        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String d = dateFormat.format(date);
        String timeStamp = timeFormat.format(date);
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

        Query roomQuery = new Query();
        roomQuery.addCriteria(Criteria.where("roomId").is(roomId));
        Update update = new Update();
        update.set("lastMessageId", message.getMessageId());
        mongoTemplate.findAndModify(roomQuery, update, Room.class, ROOM_COLLECTION);

        ChatInfo chatInfo = new ChatInfo(null, roomId, message.getMessageId(), senderId, false, true, false, false, false, false, false, false);
        mongoTemplate.save(chatInfo, CHATINFO_COLLECTION);

        updateRoomSeq(roomId, senderId);

        Room room = getRoom(roomId);
        for (Long roomUserId : room.getUserList()) {
            if (roomUserId != senderId) {
                ChatInfo tmp = new ChatInfo(null, roomId, message.getMessageId(), roomUserId, false, true, false, false, false, false, false, false);
                mongoTemplate.save(tmp, CHATINFO_COLLECTION);
                updateRoomSeq(roomId, roomUserId);
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
    public List<RoomUser> getRoomUser(long roomId, boolean needStatus) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
        List<RoomUser> res = new ArrayList<>();
        for (Long i : room.getUserList()) {
            Query userQuery = new Query(Criteria.where("userId").is(i));
            User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
            ////todo change this
            String avatar = "http://192.168.1.143:9090/vavatar/" + user.getUserId() + "?time=" + "time";
            Status status = null;
            if (needStatus) {
                status = user.getStatus();
            }
            RoomUser roomUser = new RoomUser(user.getUserId(), user.getUserName(), avatar, status);
            res.add(roomUser);
        }
        return res;
    }

    @Override
    public ParamDTO fetchmessages(long userId, long roomId, long startId) {
        ParamDTO paramDTO = new ParamDTO();
        List<MessageDTO> list = new ArrayList<>();
        paramDTO.setMessageEnd(false);

        Query roomInfoQuery = new Query();
        roomInfoQuery.addCriteria(Criteria.where("userId").is(userId));
        roomInfoQuery.addCriteria(Criteria.where("roomId").is(roomId));
        RoomInfo roomInfo = mongoTemplate.findOne(roomInfoQuery, RoomInfo.class, ROOMINFO_COLLECTION);

        if (startId == 0L) {
            startId = Integer.MAX_VALUE;
        }
        Query messageQuery = new Query(Criteria.where("roomId").is(roomId));
        messageQuery.addCriteria(Criteria.where("messageId").lt(startId).andOperator(Criteria.where("messageId").gte(roomInfo.getStartIndex())));
        messageQuery.with(Sort.by(Sort.Direction.DESC, "messageId"));
        messageQuery.limit(20);
        List<Message> messages = mongoTemplate.find(messageQuery, Message.class, MESSAGE_COLLECTION);
        Collections.reverse(messages);

        Query chatInfoQuery = new Query(Criteria.where("roomId").is(roomId));
        chatInfoQuery.addCriteria(Criteria.where("userId").is(userId));
        chatInfoQuery.addCriteria(Criteria.where("messageId").lt(startId).andOperator(Criteria.where("messageId").gte(roomInfo.getStartIndex())));
        chatInfoQuery.with(Sort.by(Sort.Direction.DESC, "messageId"));
        chatInfoQuery.limit(20);
        List<ChatInfo> chatInfos = mongoTemplate.find(chatInfoQuery, ChatInfo.class, CHATINFO_COLLECTION);
        Collections.reverse(chatInfos);

        for (int i = 0; i < messages.size(); i++) {
            MessageDTO tmp = new MessageDTO(messages.get(i), chatInfos.get(i));
            list.add(tmp);
        }

        if (list.size() == 0 || list.get(0).get_id() == roomInfo.getStartIndex() || list.get(0).get_id() == 1L) {
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

        Query roomInfoQuery = new Query();
        roomInfoQuery.addCriteria(Criteria.where("roomId").is(roomId));
        roomInfoQuery.addCriteria(Criteria.where("userId").is(userId));
        RoomInfo roomInfo = mongoTemplate.findOne(roomInfoQuery, RoomInfo.class, ROOMINFO_COLLECTION);

        if (message.getSenderId() == -1) {
            query = new Query();
            query.addCriteria(Criteria.where("roomId").is(roomId));
            query.addCriteria(Criteria.where("senderId").ne(-1));
            query.addCriteria(Criteria.where("messageId").lt(messageId));
            query.with(Sort.by(Sort.Direction.DESC, "messageId"));
            query.limit(1);
            message = mongoTemplate.findOne(query, Message.class, MESSAGE_COLLECTION);
        }

        if (userId == message.getSenderId()) {
            return null;
        }

        if (roomInfo.getStartIndex() > message.getMessageId()) {
            return null;
        }

        Query chatInfoQuery = new Query();
        chatInfoQuery.addCriteria(Criteria.where("roomId").is(roomId));
        chatInfoQuery.addCriteria(Criteria.where("messageId").is(message.getMessageId()));
        chatInfoQuery.addCriteria(Criteria.where("userId").is(message.getSenderId()));
        ChatInfo chatInfo = mongoTemplate.findOne(chatInfoQuery, ChatInfo.class, CHATINFO_COLLECTION);

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

    @Override
    public void updateRoomSeq(long roomId, long userId) {
        if (roomId == Long.MAX_VALUE) {
            return;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("roomId").is(roomId));
//        RoomInfo roomInfo = mongoTemplate.findOne(query, RoomInfo.class, ROOMINFO_COLLECTION);
//        if (roomInfo.getIndex() == 2L) {
//            return;
//        }
        Update update = new Update();
        update.set("index", new Date().getTime());
        mongoTemplate.findAndModify(query, update, RoomInfo.class, ROOMINFO_COLLECTION);

//        Query otherQuery = new Query();
//        otherQuery.addCriteria(Criteria.where("userId").is(userId));
//        otherQuery.addCriteria(Criteria.where("roomId").ne(roomId).andOperator(Criteria.where("roomId").ne(1L)));
//        Update otherUpdate = new Update();
//        otherUpdate.inc("index", 1);
//        mongoTemplate.updateMulti(otherQuery, otherUpdate, RoomInfo.class, ROOMINFO_COLLECTION);
    }

    @Override
    public long incRoomInfoUnread(long roomId, long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        query.addCriteria(Criteria.where("userId").is(userId));
        RoomInfo roomInfo = mongoTemplate.findOne(query, RoomInfo.class, ROOMINFO_COLLECTION);
        Update update = new Update();
        update.inc("unread", 1);
        mongoTemplate.findAndModify(query, update, RoomInfo.class, ROOMINFO_COLLECTION);
        return roomInfo.getUnread() + 1;
    }

    @Override
    public void clearRoomInfoUnread(long roomId, long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        query.addCriteria(Criteria.where("userId").is(userId));
        Update update = new Update();
        update.set("unread", 0);
        mongoTemplate.findAndModify(query, update, RoomInfo.class, ROOMINFO_COLLECTION);
    }

    @Override
    public long getRoomInfoUnread(long roomId, long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        query.addCriteria(Criteria.where("userId").is(userId));
        RoomInfo roomInfo = mongoTemplate.findOne(query, RoomInfo.class, ROOMINFO_COLLECTION);
        return roomInfo.getUnread();
    }

    @Override
    public RoomDTO roomToDTO(Room room, RoomInfo roomInfo, long uId, long unread) {

        ////todo change ip
        RoomDTO res = new RoomDTO();
        res.setRoomId(room.getRoomId());
        res.setRoomName(room.getRoomName());
        res.setAvatar("http://192.168.1.143:9090/vavatar/" + room.getAvatar() + "?time=" + "time");
        res.setUnreadCount(roomInfo.getUnread());
        res.setIndex(roomInfo.getIndex());

        if (room.getLastMessageId() == null) {
            res.setLastMessage(null);
        } else {
            res.setLastMessage(getLastMessage(room.getRoomId(), room.getLastMessageId(), uId, unread));
        }

        List<RoomUser> list = new ArrayList<>();
        for (Long userId : room.getUserList()) {
            RoomUser tmp = new RoomUser();
            Query query = new Query(Criteria.where("userId").is(userId));
            User user = mongoTemplate.findOne(query, User.class, UserServiceImpl.USER_COLLECTION);
            tmp.set_id(userId);
            tmp.setUsername(user.getUserName());
            ////todo change ip
            tmp.setAvatar("http://192.168.1.143:9090/vavatar/" + tmp.get_id() + "?time=" + "time");
            tmp.setStatus(user.getStatus());
            list.add(tmp);
        }

        res.setUsers(list);

        return res;
    }


    @Override
    public ChatParamDTO getUnreadDTO(long roomId, long unread, long userId) {
        ChatParamDTO chatParamDTO = new ChatParamDTO();
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        RoomInfo roomInfo = mongoTemplate.findOne(query, RoomInfo.class, ChatServiceImpl.ROOMINFO_COLLECTION);

        Query roomQuery = new Query();
        roomQuery.addCriteria(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(roomQuery, Room.class, ROOM_COLLECTION);
        LastMessage last = getLastMessage(roomId, room.getLastMessageId(), userId, unread);

        chatParamDTO.setRoomId(roomId);
        chatParamDTO.setIndex(roomInfo.getIndex());
        chatParamDTO.setUnreadCount(unread);
        chatParamDTO.setLastMessage(last);

        return chatParamDTO;
    }

    @Override
    public LastMessage getLastMessage(long roomId, long lastId, long userId, long unread) {
        Query lastQuery = new Query();
        lastQuery.addCriteria(Criteria.where("roomId").is(roomId));
        lastQuery.addCriteria(Criteria.where("messageId").is(lastId));
        Message lastMessage = mongoTemplate.findOne(lastQuery, Message.class, MESSAGE_COLLECTION);
        lastQuery.addCriteria(Criteria.where("userId").is(userId));
        ChatInfo chatInfo = mongoTemplate.findOne(lastQuery, ChatInfo.class, CHATINFO_COLLECTION);
        LastMessage last = new LastMessage(lastMessage, chatInfo, unread != 0);
        return last;
    }

    @Override
    public ChatParamDTO splitRoomUser(Long roomId, Long userId) {
        ChatParamDTO res = new ChatParamDTO();
        List<RoomUser> allUser = getRoomUser(1L, false);
        List<RoomUser> userList = new ArrayList<>();
        List<RoomUser> userAlreadyInRoomList = new ArrayList<>();
        if (roomId == null) {
            Query userQuery = new Query(Criteria.where("userId").is(userId));
            User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
            ////todo change this
            String avatar = "http://192.168.1.143:9090/vavatar/" + user.getUserId() + "?time=" + "time";
            RoomUser roomUser = new RoomUser(user.getUserId(), user.getUserName(), avatar, null);
            allUser.removeIf(u -> u.get_id() == userId);
            userAlreadyInRoomList.add(roomUser);
            userList = allUser;
        } else {
            Query query = new Query();
            query.addCriteria(Criteria.where("roomId").is(roomId));
            Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
            List<Long> userIds = room.getUserList();
            for (RoomUser roomUser : allUser) {
                if (userIds.contains(roomUser.get_id())) {
                    userAlreadyInRoomList.add(roomUser);
                } else {
                    userList.add(roomUser);
                }
            }
        }
        res.setUserList(userList);
        res.setUserAlreadyInRoomList(userAlreadyInRoomList);
        return res;
    }

    @Override
    public List<Long> deleteRoom(long roomId, long userId) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
        if (room.getUserList().contains(userId)) {
            mongoTemplate.remove(query, ROOM_COLLECTION);
            mongoTemplate.remove(query, MESSAGE_COLLECTION);
            mongoTemplate.remove(query, CHATINFO_COLLECTION);
            mongoTemplate.remove(query, ROOMINFO_COLLECTION);
            return room.getUserList();
        } else {
            return null;
        }
    }

    @Override
    public List<Long> removeUserFromUser(long roomId, long userId) {
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
        if (room.getUserList().contains(userId)) {
            room.getUserList().remove(userId);
        }
        Update update = new Update();
        update.set("userList", room.getUserList());
        mongoTemplate.findAndModify(query, update, Room.class, ROOM_COLLECTION);
        query.addCriteria(Criteria.where("userId").is(userId));
//        mongoTemplate.remove(query, CHATINFO_COLLECTION);
        mongoTemplate.remove(query, ROOMINFO_COLLECTION);
        return room.getUserList();
    }

    @Override
    public MessageDTO sendSysMessage(Long roomId, String content) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String d = dateFormat.format(date);
        String timeStamp = timeFormat.format(date);

        Message message = new Message(
                null,
                getNextMessageId(roomId),
                roomId,
                -1,
                content,
                "system",
                null,
                d,
                timeStamp,
                null,
                null,
                new Reactions(null, null));
        mongoTemplate.save(message, MESSAGE_COLLECTION);

        Query roomQuery = new Query();
        roomQuery.addCriteria(Criteria.where("roomId").is(roomId));
        Update update = new Update();
        update.set("lastMessageId", message.getMessageId());
        mongoTemplate.findAndModify(roomQuery, update, Room.class, ROOM_COLLECTION);

        ChatInfo chatInfo = new ChatInfo(null, roomId, message.getMessageId(), null, true, true, true, true, false, false, true, true);
        Room room = getRoom(roomId);
        for (Long roomUserId : room.getUserList()) {
            ////todo
            ChatInfo tmp = new ChatInfo(null, roomId, message.getMessageId(), roomUserId, true, true, true, true, false, false, true, true);
            mongoTemplate.save(tmp, CHATINFO_COLLECTION);
            updateRoomSeq(roomId, roomUserId);
        }

        MessageDTO res = new MessageDTO(message, chatInfo);
        return res;
    }

    @Override
    public Long addUserToRoom(long roomId, List<Long> newUserList, List<Long> addedUser) {
        Long res = roomId;
        if (roomId == 0L) {
            Room room = addRoom(newUserList, newRoomName(newUserList));
            res = room.getRoomId();
        } else {
            Query query = new Query();
            query.addCriteria(Criteria.where("roomId").is(roomId));
            Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
            Update update = new Update();
            update.set("userList", newUserList);
            mongoTemplate.findAndModify(query, update, Room.class, ROOM_COLLECTION);
            for (Long newUser : addedUser) {
                RoomInfo roomInfo = new RoomInfo(null, roomId, newUser, 0L, new Date().getTime(), room.getNextMessageId());
                mongoTemplate.save(roomInfo, ROOMINFO_COLLECTION);
            }
        }
        return res;
    }

    @Override
    public RoomDTO getRoomDTO(long userId, long roomId) {
        Query roomInfoQuery = new Query();
        roomInfoQuery.addCriteria(Criteria.where("userId").is(userId));
        roomInfoQuery.addCriteria(Criteria.where("roomId").is(roomId));
        RoomInfo roomInfo = mongoTemplate.findOne(roomInfoQuery, RoomInfo.class, ROOMINFO_COLLECTION);

        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomInfo.getRoomId()));
        Room room = mongoTemplate.findOne(query, Room.class, ROOM_COLLECTION);
        RoomDTO tmp = roomToDTO(room, roomInfo, userId, roomInfo.getUnread());
        return tmp;
    }

    @Override
    public String welcoming(List<Long> userList) {
        StringBuilder sb = new StringBuilder();
        sb.append("欢迎: ");
        for (int i = 0; i < userList.size(); i++) {
            long userId = userList.get(i);
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(userId));
            User user = mongoTemplate.findOne(query, User.class, UserServiceImpl.USER_COLLECTION);
            sb.append(user.getUserName());
            if (i < userList.size() - 1) {
                sb.append(", ");
            } else {
                sb.append("加入");
            }
        }
        return sb.toString();
    }

    @Override
    public String newRoomName(List<Long> userList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < userList.size(); i++) {
            long userId = userList.get(i);
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(userId));
            User user = mongoTemplate.findOne(query, User.class, UserServiceImpl.USER_COLLECTION);
            sb.append(user.getUserName());
            if (i < userList.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


}
