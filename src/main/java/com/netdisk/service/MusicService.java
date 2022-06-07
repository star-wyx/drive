package com.netdisk.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.netdisk.module.DTO.SongDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Song;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface MusicService {

    /**
     * 从数据库中读取Song记录
     */
    Song getSong(String md5);

    /**
     * 往数据库添加Song
     */
    SongDTO setSong(FileNode fileNode, String token);

    /**
     * 获取同一用户的所有Clinet
     */
    List<SocketIOClient> getClients(long userId, ConcurrentHashMap<SocketIOClient, Long> webSocketMap);

    /**
     * 获取用户的songList
     */
    List<SongDTO> getSongList(long userId, String token);

}
