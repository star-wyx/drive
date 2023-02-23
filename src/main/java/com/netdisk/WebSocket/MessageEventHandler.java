package com.netdisk.WebSocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.netdisk.module.DTO.*;
import com.netdisk.module.FileNode;
import com.netdisk.module.Song;
import com.netdisk.module.User;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomUser;
import com.netdisk.module.chat.Status;
import com.netdisk.service.ChatService;
import com.netdisk.service.MusicService;
import com.netdisk.service.impl.ChatServiceImpl;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.MusicServiceImpl;
import com.netdisk.service.impl.UserServiceImpl;
import com.netdisk.util.MyFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息事件，作为后端与前台交互
 *
 * @authoer liangxifeng 2018-07-07
 */
@Component
@Slf4j
public class MessageEventHandler {
    public static SocketIOServer socketIoServer;
    static ArrayList<UUID> listClient = new ArrayList<UUID>();
    static final int limitSeconds = 60;
    static Map<Long, UUID> map = new HashMap<>();
    //线程安全的map
    public static ConcurrentHashMap<SocketIOClient, Long> client_id = new ConcurrentHashMap<SocketIOClient, Long>();
    public static ConcurrentHashMap<Long, List<SocketIOClient>> id_client = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<SocketIOClient, Long> socket_inRoom = new ConcurrentHashMap<>();

    @Autowired
    public MessageEventHandler(SocketIOServer server) {
        this.socketIoServer = server;
    }

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MusicService musicService;

    @Autowired
    ChatService chatService;

    @Autowired
    MyFileUtils myFileUtils;

    /**
     * 客户端连接的时候触发，前端js触发：socket = io.connect("http://192.168.9.209:9092");
     *
     * @param client
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
//        String mac = client.getHandshakeData().getSingleUrlParam("mac");
//        以mac地址为key,SocketIOClient 为value存入map,后续可以指定mac地址向客户端发送消息
//        webSocketMap.put(mac,client);
//        socketIoServer.getClient(client.getSessionId()).sendEvent("message", "back data");
//        sendBuyLogEvent();

//        Long userId = Long.valueOf(client.getHandshakeData().getSingleUrlParam("user_id"));
//        webSocketMap.put(client, userId);
        System.out.println("客户端:" + client.getSessionId() + "连接成功");
    }

    /**
     * 客户端关闭连接时触发：前端js触发：socket.disconnect();
     *
     * @param client
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        Long userId = client_id.get(client);
        if (userId == null) {
            System.out.println("注册时连接，断开");
            return;
        }
        client_id.remove(client);
        socket_inRoom.remove(client);

        List<SocketIOClient> list = id_client.get(userId);
        list.remove(client);
        if (list == null || list.size() == 0) {
            id_client.remove(userId);
            Query userQuery = new Query(Criteria.where("userId").is(userId));
//            User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
            Update update = new Update();
            SimpleDateFormat format = new SimpleDateFormat("yyyy 年 MM 月 dd 日 E HH 点 mm 分");
            Status newStatue = new Status("offline", format.format(new Date()));
//            user.setStatus(newStatue);
            update.set("status", newStatue);
            mongoTemplate.findAndModify(userQuery, update, User.class, UserServiceImpl.USER_COLLECTION);

            RoomUser roomUser = chatService.getRoomUser(userId);
            for (SocketIOClient socket : client_id.keySet()) {
                socket.sendEvent("userStatusChanged", roomUser);
            }

        } else {
            id_client.put(userId, list);
        }
        System.out.println("客户端:" + client.getSessionId() + "userId: " + userId + "断开连接");
    }

    /**
     * 自定义消息事件，客户端js触发：socket.emit('messageevent', {msgContent: msg}); 时触发
     * 前端js的 socket.emit("事件名","参数数据")方法，是触发后端自定义消息事件的时候使用的,
     * 前端js的 socket.on("事件名",匿名函数(服务器向客户端发送的数据))为监听服务器端的事件
     */
    @OnEvent(value = "getUserId")
    public void getUserId(SocketIOClient client, AckRequest request, Long user_id, String token) {
//        服务器端向该客户端发送消息
//        socketIoServer.getClient(client.getSessionId()).sendEvent("messageevent", "你好 data");
//        client.sendEvent("message", "我是服务器都安发送的信息");

        Long userId = Long.valueOf(user_id);
        client_id.put(client, userId);

        // 获取userId下的所有socket client， 如果这是user当前第一个在线的client，更新用户status
        List<SocketIOClient> list = id_client.get(userId);
        if (list == null || list.size() == 0) {
            list = new ArrayList<SocketIOClient>();
            Query userQuery = new Query(Criteria.where("userId").is(user_id));
            User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
            Update update = new Update();
            user.getStatus().setState("online");
            update.set("status", user.getStatus());
            mongoTemplate.findAndModify(userQuery, update, User.class, UserServiceImpl.USER_COLLECTION);

            RoomUser roomUser = chatService.getRoomUser(userId);
            for (SocketIOClient socket : client_id.keySet()) {
                socket.sendEvent("userStatusChanged", roomUser);
            }

        }
        if (!list.contains(client)) {
            list.add(client);
        }
        id_client.put(userId, list);


        System.out.println("userId: " + userId + " 连接成功");
        client.sendEvent("songList", musicService.getSongList(userId, token));
    }

    // 添加一首新歌
    @OnEvent(value = "addNewSong")
    public void addNewSong(SocketIOClient client, AckRequest request, Long nodeId, String token, Long song_userId) {
        Long list_userId = client_id.get(client);
//        Query userQuery = new Query();
//        userQuery.addCriteria(Criteria.where("userId").is(userId));
//        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
        Query fileQuery = new Query();
        fileQuery.addCriteria(Criteria.where("nodeId").is(nodeId));
        fileQuery.addCriteria(Criteria.where("userId").is(song_userId));
        FileNode fileNode = mongoTemplate.findOne(fileQuery, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        SongDTO songDTO = musicService.setSong(fileNode, token, list_userId);
        List<SocketIOClient> clients = musicService.getClients(list_userId, client_id);
        for (SocketIOClient c : clients) {
            c.sendEvent("newSongAdded", songDTO);
        }
    }

    // 添加多个新歌
    @OnEvent(value = "addNewSongs")
    public void addNewSongs(SocketIOClient client, AckRequest request, List<Integer> nodeIds, String token, Long userId) {
        Long list_userId = client_id.get(client);
//        Query userQuery = new Query();
//        userQuery.addCriteria(Criteria.where("userId").is(userId));
//        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
        List<SongDTO> res = new ArrayList<>();
        for (int i = 0; i < nodeIds.size(); i++) {
            long nodeId = (long) nodeIds.get(i);
            Query fileQuery = new Query();
            fileQuery.addCriteria(Criteria.where("nodeId").is(nodeId));
            fileQuery.addCriteria(Criteria.where("userId").is(userId));
            FileNode fileNode = mongoTemplate.findOne(fileQuery, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            SongDTO songDTO = musicService.setSong(fileNode, token, list_userId);
            res.add(songDTO);
        }
        List<SocketIOClient> clients = musicService.getClients(list_userId, client_id);
        for (SocketIOClient c : clients) {
            c.sendEvent("newSongsAdded", res);
        }
    }

    // 新增分享歌曲
    @OnEvent(value = "addNewSongsShared")
    public void addNewSongsShared(SocketIOClient client, AckRequest request, List nodeIds, String token) {
        Long list_userId = client_id.get(client);
        long userId = 0;
//        Query userQuery = new Query();
//        userQuery.addCriteria(Criteria.where("userId").is(userId));
//        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
        List<SongDTO> res = new ArrayList<>();
        for (int i = 0; i < nodeIds.size(); i++) {
            LinkedHashMap map = (LinkedHashMap) nodeIds.get(i);
            Query fileQuery = new Query();
            fileQuery.addCriteria(Criteria.where("nodeId").is(map.get("node_id")));
            fileQuery.addCriteria(Criteria.where("userId").is(map.get("user_id")));
            FileNode fileNode = mongoTemplate.findOne(fileQuery, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            SongDTO songDTO = musicService.setSong(fileNode, token, list_userId);
            res.add(songDTO);
        }
        List<SocketIOClient> clients = musicService.getClients(list_userId, client_id);
        for (SocketIOClient c : clients) {
            c.sendEvent("newSongsAdded", res);
        }
    }

    // 删除歌曲
    @OnEvent(value = "deleteSong")
    public void deleteSong(SocketIOClient client, AckRequest request, Long nodeId, Long userId) {
        long list_userId = client_id.get(client);
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("list_userId").is(list_userId));
        mongoTemplate.findAndRemove(query, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
        List<SocketIOClient> clients = musicService.getClients(list_userId, client_id);
        for (SocketIOClient c : clients) {
            c.sendEvent("songDeleted", nodeId, userId);
        }
    }

    // 更改分享状态
    @OnEvent(value = "changeShare")
    public void changeShare(SocketIOClient client, AckRequest request, Long nodeId, Long userId, Boolean isShared) {
        long list_userId = client_id.get(client);
        if (list_userId != userId) {
            return;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("list_userId").is(list_userId));
//        Song song = mongoTemplate.findOne(query, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
        Update update = new Update();
        update.set("isShared", isShared);
        mongoTemplate.findAndModify(query, update, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
    }

    // 更改喜爱状态
    @OnEvent(value = "changeFav")
    public void changeFav(SocketIOClient client, AckRequest request, Long nodeId, Long userId, Boolean isFav) {
        long list_userId = client_id.get(client);
        if (list_userId != userId) {
            return;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("list_userId").is(list_userId));
//        Song song = mongoTemplate.findOne(query, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
        Update update = new Update();
        update.set("isFavorites", isFav);
        mongoTemplate.findAndModify(query, update, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
    }


    public void sendBuyLogEvent(long id) {   //这里就是向客户端推消息了
        //String dateTime = new DateTime().toString("hh:mm:ss");
        socketIoServer.getClient(map.get(id)).sendEvent("message", "hello world!");
    }


    // 新增聊天室
    @OnEvent(value = "initialRoomBegin")
    public void initialRoomBegin(SocketIOClient client, AckRequest request) {
        client.sendEvent("initialRoom", chatService.getAllRoom(client_id.get(client)));
        if (chatService.checkAtMe(client_id.get(client))) {
            client.sendEvent("attedMe", true);
        }
    }

    // 发送message
    @OnEvent(value = "sendMessage")
    public void sendMessage(SocketIOClient client, AckRequest request, Long roomId, String content, byte[] files,
                            List<Integer> _replyMessage, List<Integer> _usersTag, Long senderId, Long dummyId) {
        List<Long> replyMessage = myFileUtils.IntegerToLong(_replyMessage);
        List<Long> usersTag = myFileUtils.IntegerToLong(_usersTag);
        MessageDTO messageDTO = chatService.addMessageToRoom(roomId, content, null, replyMessage, usersTag, senderId);

        // 获得sender的所有client
        List<SocketIOClient> senderSocket = id_client.get(senderId);
        for (SocketIOClient socketIOClient : senderSocket) {
            if (socket_inRoom.get(socketIOClient) == null) {
                continue;
            }
            if (socketIOClient != client && socket_inRoom.get(socketIOClient) == messageDTO.getRoomId()) {
                socketIOClient.sendEvent("senderNewMessageAdded", messageDTO);
            } else if (socket_inRoom.get(socketIOClient) == messageDTO.getRoomId()) {
                ChatParamDTO chatParamDTO = new ChatParamDTO();
                chatParamDTO.set_id(messageDTO.get_id());
                chatParamDTO.setFiles(null);
                chatParamDTO.setDummyId(dummyId);
                chatParamDTO.setRoomId(roomId);
                client.sendEvent("senderNewMessageSaved", chatParamDTO);
            }
        }

        // 获得聊天室的所有user
        List<RoomUser> roomUsers = chatService.getRoomUser(roomId, true);
        messageDTO.setDistributed(true);
        for (RoomUser roomUser : roomUsers) {
            List<SocketIOClient> tmp = id_client.get(roomUser.get_id());
            if (tmp == null || tmp.size() == 0) {
                continue;
            }
            if (roomUser.get_id() != senderId) {
                // 非发送方
                boolean isAt = chatService.checkAtMe(roomUser.get_id());
                for (SocketIOClient socketIOClient : tmp) {
                    // 如果用户正在该房间
                    if (socket_inRoom.get(socketIOClient) != null && socket_inRoom.get(socketIOClient) == messageDTO.getRoomId()) {
                        socketIOClient.sendEvent("newMessageAdded", messageDTO);
                        socketIOClient.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, 0, roomUser.get_id()));
                        chatService.changeIsAt(roomUser.get_id(), roomId, false);
                    } else {
                        // 如果用户不在该房间
                        long unread = chatService.incRoomInfoUnread(roomId, roomUser.get_id());
                        socketIOClient.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, unread, roomUser.get_id()));
                    }

                    // 如果被at
                    if (isAt) {
                        socketIOClient.sendEvent("attedMe", true);
                    }

                }
            } else {
                // 发送方
                for (SocketIOClient socketIOClient : tmp) {
                    socketIOClient.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, 0, roomUser.get_id()));
                    chatService.changeIsAt(roomUser.get_id(), roomId, false);

                }
            }
            chatService.changeDistributed(roomUser.get_id(), roomId, messageDTO.get_id(), true);
        }

        for (SocketIOClient socketIOClient : senderSocket) {
            if (socket_inRoom.get(socketIOClient) == messageDTO.getRoomId()) {
                socketIOClient.sendEvent("distributeFinished", roomId, messageDTO.get_id());
            }
        }
    }

    @OnEvent(value = "fetchmessages")
    public void fetchmessages(SocketIOClient client, AckRequest request, Long roomId, Long messageId) {
        long userId = client_id.get(client);
        ParamDTO res = chatService.fetchmessages(userId, roomId, messageId);
        client.sendEvent("renewMessage", res);
    }

    @OnEvent(value = "iSeen")
    public void iSeen(SocketIOClient client, AckRequest request, Long roomId, Long messageId, Long userId) {
        ChatParamDTO chatParamDTO = chatService.iSeen(roomId, messageId, userId);
        if (chatParamDTO != null) {
            long senderId = chatParamDTO.getUserId();
            chatParamDTO.setUserId(null);
            List<SocketIOClient> list = id_client.get(senderId);
            for (SocketIOClient socketIOClient : list) {
                if (socket_inRoom.get(socketIOClient) == chatParamDTO.getRoomId()) {
                    socketIOClient.sendEvent("messageSeen", chatParamDTO);
                }
            }
        }
    }

    // 用户进入一个room
    @OnEvent(value = "rightNowRoom")
    public void rightNowRoom(SocketIOClient client, AckRequest request, Long roomId) {
        log.info("rightNowRoom");
        socket_inRoom.put(client, roomId);
        if (roomId != null) {
            chatService.clearRoomInfoUnread(roomId, client_id.get(client));
        }
    }

    // 新建room时的用户列表
    @OnEvent(value = "newRoomUserList")
    public void newRoomUserList(SocketIOClient client, AckRequest request) {
        client.sendEvent("userListAccessed", chatService.splitRoomUser(null, client_id.get(client)));
    }

    // 向room邀请用户
    @OnEvent(value = "inviteUserList")
    public void inviteUserList(SocketIOClient client, AckRequest request, Long roomId) {
        client.sendEvent("userListAccessed", chatService.splitRoomUser(roomId, client_id.get(client)));
    }

    @OnEvent(value = "deleteRoom")
    public void deleteRoom(SocketIOClient client, AckRequest request, Long roomId) {
        List<Long> userList = chatService.deleteRoom(roomId, client_id.get(client));
        for (Long userId : userList) {
            List<SocketIOClient> sockets = id_client.get(userId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    socket.sendEvent("roomDeleted", roomId);
                }
            }
        }
    }

    @OnEvent(value = "removeUserFromRoom")
    public void removeUserFromRoom(SocketIOClient client, AckRequest request, Long roomId) {
        long userId = client_id.get(client);
        List<Long> userList = chatService.removeUserFromUser(roomId, userId);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        User user = mongoTemplate.findOne(query, User.class, UserServiceImpl.USER_COLLECTION);
        MessageDTO messageDTO = chatService.sendSysMessage(roomId, "通知: " + user.getUserName() + "已退出该房间");
        for (Long uId : userList) {
            RoomDTO roomDTO = chatService.getRoomDTO(uId, roomId);
            ChatParamDTO res = new ChatParamDTO();
            res.setRoomId(roomDTO.getRoomId());
            res.setNewRoomName(roomDTO.getRoomName());
            res.setUsers(roomDTO.getUsers());

            List<SocketIOClient> sockets = id_client.get(uId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    if (socket_inRoom.get(socket) == messageDTO.getRoomId()) {
                        socket.sendEvent("newMessageAdded", messageDTO);
                        socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, 0, uId));
                    } else {
                        long unread = chatService.incRoomInfoUnread(roomId, uId);
                        socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, unread, uId));
                    }
                    socket.sendEvent("roomInfoChange", res);
                }
            }
        }

        List<SocketIOClient> socketIOClients = id_client.get(userId);
        for (SocketIOClient socket : socketIOClients) {
            socket.sendEvent("roomDeleted", roomId);
        }
    }

    @OnEvent(value = "roomUserChanged")
    public void roomUserChanged(SocketIOClient client, AckRequest request, List<Integer> _newUserList, Long roomId, List<Integer> _addedUser, List<Integer> _oldUserList) {
        List<Long> newUserList = myFileUtils.IntegerToLong(_newUserList);
        List<Long> addedUser = myFileUtils.IntegerToLong(_addedUser);
        List<Long> oldUserList = myFileUtils.IntegerToLong(_oldUserList);

        Long newRoomId = chatService.addUserToRoom(roomId, newUserList, addedUser);
        if (roomId == 0L) {
            for (Long userId : newUserList) {
                RoomDTO roomDTO = chatService.getRoomDTO(userId, newRoomId);
                List<SocketIOClient> sockets = id_client.get(userId);
                if (sockets != null && sockets.size() != 0) {
                    for (SocketIOClient socket : sockets) {
                        socket.sendEvent("newRoomAdded", roomDTO);
                    }
                }
            }
        } else {
            MessageDTO messageDTO = chatService.sendSysMessage(roomId, chatService.welcoming(addedUser));

            for (Long userId : oldUserList) {
                RoomDTO roomDTO = chatService.getRoomDTO(userId, roomId);
                ChatParamDTO res = new ChatParamDTO();
                res.setRoomId(roomDTO.getRoomId());
                res.setNewRoomName(roomDTO.getRoomName());
                res.setUsers(roomDTO.getUsers());
                List<SocketIOClient> sockets = id_client.get(userId);
                if (sockets != null && sockets.size() != 0) {
                    for (SocketIOClient socket : sockets) {
                        if (socket_inRoom.get(socket) == messageDTO.getRoomId()) {
                            socket.sendEvent("newMessageAdded", messageDTO);
                            socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, 0, userId));
                        } else {
                            long unread = chatService.incRoomInfoUnread(roomId, userId);
                            socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, unread, userId));
                        }
                        socket.sendEvent("roomInfoChange", res);
                    }
                }
            }

            for (Long userId : addedUser) {
                RoomDTO roomDTO = chatService.getRoomDTO(userId, roomId);
                ChatParamDTO res = new ChatParamDTO();
                res.setRoomId(roomDTO.getRoomId());
                res.setNewRoomName(roomDTO.getRoomName());
                res.setUsers(roomDTO.getUsers());
                List<SocketIOClient> sockets = id_client.get(userId);
                if (sockets != null && sockets.size() != 0) {
                    for (SocketIOClient socket : sockets) {
                        socket.sendEvent("newRoomAdded", chatService.getRoomDTO(userId, roomId));
                        if (socket_inRoom.get(socket) == messageDTO.getRoomId()) {
                            socket.sendEvent("newMessageAdded", messageDTO);
                            socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, 0, userId));
                        } else {
                            long unread = chatService.incRoomInfoUnread(roomId, userId);
                            socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(roomId, unread, userId));
                        }
                        socket.sendEvent("roomInfoChange", res);
                    }
                }
            }
        }
    }

    @OnEvent("deleteMessage")
    public void deleteMessage(SocketIOClient client, AckRequest request, Long roomId, Long messageId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ChatServiceImpl.ROOM_COLLECTION);
        List<Long> userList = room.getUserList();
        chatService.deleteMessage(roomId, messageId);
        for (Long userId : userList) {
            List<SocketIOClient> sockets = id_client.get(userId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    if (socket_inRoom.get(socket) == roomId) {
                        socket.sendEvent("messageDeleted", messageId);
                    }
                }
            }
        }
    }

    @OnEvent("editMessage")
    public void editMessage(SocketIOClient client, AckRequest request, Long roomId, Long messageId, String content,
                            List<Integer> _replyMessage, List<Integer> _usersTag, Long senderId, Long dummyId) {
        List<Long> replyMessage = myFileUtils.IntegerToLong(_replyMessage);
        List<Long> usersTag = myFileUtils.IntegerToLong(_usersTag);
        MessageDTO messageDTO = chatService.editMessage(roomId, messageId, content, replyMessage, usersTag, client_id.get(client));

        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ChatServiceImpl.ROOM_COLLECTION);
        List<Long> userList = room.getUserList();
        for (Long userId : userList) {
            List<SocketIOClient> sockets = id_client.get(userId);
            boolean isAt = chatService.checkAtMe(userId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    if (isAt) {
                        socket.sendEvent("attedMe", true);
                        socket.sendEvent("editAtYou", roomId);
                    }
                    if (socket_inRoom.get(socket) == messageDTO.getRoomId()) {
                        socket.sendEvent("messageEdited", messageDTO);
                    }
                }
            }
        }
    }

    @OnEvent("roomNameChanged")
    public void roomNameChanged(SocketIOClient client, Long roomId, String name) {
        List<Long> userList = chatService.roomNameChanged(roomId, name);
        for (Long userId : userList) {
            RoomDTO roomDTO = chatService.getRoomDTO(userId, roomId);
            ChatParamDTO res = new ChatParamDTO();
            res.setRoomId(roomDTO.getRoomId());
            res.setRoomName(roomDTO.getRoomName());
            List<SocketIOClient> sockets = id_client.get(userId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    socket.sendEvent("changedRoomName", res);
                }
            }
        }
    }

    @OnEvent("sendReaction")
    public void sendReaction(SocketIOClient client, Long roomId, Long messageId, String reaction, Boolean isRemove) {
        Long userId = client_id.get(client);
        Map<String, List> reactions = chatService.sendReaction(roomId, userId, messageId, reaction, isRemove);
        ChatParamDTO res = new ChatParamDTO();
        res.setMessageId(messageId);
        res.setReactions(reactions);
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId));
        Room room = mongoTemplate.findOne(query, Room.class, ChatServiceImpl.ROOM_COLLECTION);

        List<Long> userList = room.getUserList();
        for (Long uId : userList) {
            List<SocketIOClient> sockets = id_client.get(uId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    if (socket_inRoom.get(socket) == roomId) {
                        socket.sendEvent("reactionAdded", res);
                    }
                }
            }
        }
    }

    @OnEvent("newUserToMainRoom")
    public void newUserToMainRoom(String userName) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userName").is(userName));
        User user = mongoTemplate.findOne(query, User.class, UserServiceImpl.USER_COLLECTION);
        List<Long> addedUser = new ArrayList<>();
        addedUser.add(user.getUserId());

        Query roomQuery = new Query();
        roomQuery.addCriteria(Criteria.where("roomId").is(1L));
        Room room = mongoTemplate.findOne(roomQuery, Room.class, ChatServiceImpl.ROOM_COLLECTION);
        List<Long> oldUserList = room.getUserList();
        oldUserList.remove(user.getUserId());

        MessageDTO messageDTO = chatService.sendSysMessage(1L, chatService.welcoming(addedUser));

        for (Long userId : oldUserList) {
            RoomDTO roomDTO = chatService.getRoomDTO(userId, 1L);
            ChatParamDTO res = new ChatParamDTO();
            res.setRoomId(roomDTO.getRoomId());
            res.setNewRoomName(roomDTO.getRoomName());
            res.setUsers(roomDTO.getUsers());
            List<SocketIOClient> sockets = id_client.get(userId);
            if (sockets != null && sockets.size() != 0) {
                for (SocketIOClient socket : sockets) {
                    if (socket_inRoom.get(socket) == messageDTO.getRoomId()) {
                        socket.sendEvent("newMessageAdded", messageDTO);
                        socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(1L, 0, userId));
                    } else {
                        long unread = chatService.incRoomInfoUnread(1L, userId);
                        socket.sendEvent("unreadUpdate", chatService.getUnreadDTO(1L, unread, userId));
                    }
                    socket.sendEvent("roomInfoChange", res);
                }
            }
        }
    }

}