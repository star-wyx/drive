package com.netdisk.WebSocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.SongDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Song;
import com.netdisk.module.User;
import com.netdisk.service.MusicService;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.MusicServiceImpl;
import com.netdisk.service.impl.UserServiceImpl;
import com.netdisk.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


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
    public static ConcurrentHashMap<SocketIOClient, Long> webSocketMap = new ConcurrentHashMap<SocketIOClient, Long>();

    @Autowired
    public MessageEventHandler(SocketIOServer server) {
        this.socketIoServer = server;
    }

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MusicService musicService;

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
        Long userId = webSocketMap.get(client);
        webSocketMap.remove(client);
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
        webSocketMap.put(client, userId);
        System.out.println("userId: " + userId + " 连接成功");
        client.sendEvent("songList", musicService.getSongList(userId, token));
    }

    @OnEvent(value = "addNewSong")
    public void addNewSong(SocketIOClient client, AckRequest request, Long nodeId, String token, Long song_userId) {
        Long list_userId = webSocketMap.get(client);
//        Query userQuery = new Query();
//        userQuery.addCriteria(Criteria.where("userId").is(userId));
//        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);
        Query fileQuery = new Query();
        fileQuery.addCriteria(Criteria.where("nodeId").is(nodeId));
        fileQuery.addCriteria(Criteria.where("userId").is(song_userId));
        FileNode fileNode = mongoTemplate.findOne(fileQuery, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        SongDTO songDTO = musicService.setSong(fileNode, token, list_userId);
        List<SocketIOClient> clients = musicService.getClients(list_userId, webSocketMap);
        for (SocketIOClient c : clients) {
            c.sendEvent("newSongAdded", songDTO);
        }
    }

    @OnEvent(value = "addNewSongs")
    public void addNewSongs(SocketIOClient client, AckRequest request, List<Integer> nodeIds, String token, Long userId) {
        Long list_userId = webSocketMap.get(client);
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
        List<SocketIOClient> clients = musicService.getClients(list_userId, webSocketMap);
        for (SocketIOClient c : clients) {
            c.sendEvent("newSongsAdded", res);
        }
    }

    @OnEvent(value = "addNewSongsShared")
    public void addNewSongsShared(SocketIOClient client, AckRequest request, List nodeIds, String token) {
        Long list_userId = webSocketMap.get(client);
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
        List<SocketIOClient> clients = musicService.getClients(list_userId, webSocketMap);
        for (SocketIOClient c : clients) {
            c.sendEvent("newSongsAdded", res);
        }
    }

    @OnEvent(value = "deleteSong")
    public void deleteSong(SocketIOClient client, AckRequest request, Long nodeId, Long userId) {
        long list_userId = webSocketMap.get(client);
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("list_userId").is(list_userId));
        mongoTemplate.findAndRemove(query, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
        List<SocketIOClient> clients = musicService.getClients(list_userId, webSocketMap);
        for (SocketIOClient c : clients) {
            c.sendEvent("songDeleted", nodeId, userId);
        }
    }

    @OnEvent(value = "changeShare")
    public void changeShare(SocketIOClient client, AckRequest request, Long nodeId, Long userId, Boolean isShared) {
        long list_userId = webSocketMap.get(client);
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

    @OnEvent(value = "changeFav")
    public void changeFav(SocketIOClient client, AckRequest request, Long nodeId, Long userId, Boolean isFav) {
        long list_userId = webSocketMap.get(client);
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

}