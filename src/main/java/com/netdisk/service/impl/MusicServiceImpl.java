package com.netdisk.service.impl;

import com.corundumstudio.socketio.SocketIOClient;
import com.netdisk.WebSocket.MessageEventHandler;
import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.SongDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Song;
import com.netdisk.service.MusicService;
import com.netdisk.util.MyFileUtils;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class MusicServiceImpl implements MusicService {

    public static final String MUSIC_COLLECTION = "music";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    MyFileUtils myFileUtils;

    @Override
    public Song getSong(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query, Song.class, MUSIC_COLLECTION);
    }

    @Override
    public SongDTO setSong(FileNode fileNode, String token, long list_userId) {
        Song tmp = getSong(fileNode.getMd5());

        if (tmp != null) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(fileNode.getUserId()));
            query.addCriteria(Criteria.where("nodeId").is(fileNode.getNodeId()));
            query.addCriteria(Criteria.where("list_userId").is(list_userId));
            Song check = mongoTemplate.findOne(query, Song.class, MUSIC_COLLECTION);
            if (check != null) {
                return new SongDTO(tmp, token);
            } else {
                boolean isShared = fileNode.getUserId() == list_userId && fileNode.isShared();
                boolean isFavorites = fileNode.getUserId() == list_userId && fileNode.isFavorites();
                Song song = new Song(
                        null,
                        fileNode.getFileName(),
                        tmp.getMd5(),
                        tmp.getName(),
                        tmp.getSinger(),
                        tmp.getBase64(),
                        fileNode.getUserId(),
                        fileNode.getNodeId(),
                        list_userId,
                        isShared,
                        isFavorites);
                mongoTemplate.save(song, MUSIC_COLLECTION);
                return new SongDTO(song, token);
            }
        }

        File file = new File(fileProperties.getRootDir() + File.separator + fileNode.getStorePath());
        if (!file.exists()) {
            System.out.println("music file not exist");
        }
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            TagField binaryField = tag.getFirstField(FieldKey.COVER_ART);
            String name = (!tag.getFirst(FieldKey.TITLE).equals("") && (tag.getFirst(FieldKey.TITLE) != null)) ? tag.getFirst(FieldKey.TITLE) : fileNode.getFileName();
            String artist = (!tag.getFirst(FieldKey.ARTIST).equals("") && tag.getFirst(FieldKey.ARTIST) != null) ? tag.getFirst(FieldKey.ARTIST) : "unkown 未知";
            byte[] imageRawData = null;
            if (tag.getFirstArtwork() != null) {
                imageRawData = tag.getFirstArtwork().getBinaryData();
            } else {
                File defaultImage = new File(fileProperties.getProfileDir() + File.separator + "defaultImage.png");
                imageRawData = FileUtils.readFileToByteArray(defaultImage);
            }
            String base64 = null;
            if (imageRawData.length != 0) {
                base64 = "data:image/png;base64," + myFileUtils.getBase64(imageRawData);
            }
            boolean isShared = fileNode.getUserId() == list_userId && fileNode.isShared();
            boolean isFavorites = fileNode.getUserId() == list_userId && fileNode.isFavorites();
            Song song = new Song(
                    null,
                    fileNode.getFileName(),
                    fileNode.getMd5(),
                    name,
                    artist,
                    base64,
                    fileNode.getUserId(),
                    fileNode.getNodeId(),
                    list_userId,
                    isShared,
                    isFavorites);
            mongoTemplate.save(song, MUSIC_COLLECTION);
//            tag.getFirst(FieldKey.ALBUM);
//            tag.getFirst(FieldKey.COMMENT);
//            tag.getFirst(FieldKey.YEAR);
//            tag.getFirst(FieldKey.TRACK);
//            tag.getFirst(FieldKey.DISC_NO);
//            tag.getFirst(FieldKey.COMPOSER);
//            tag.getFirst(FieldKey.ARTIST_SORT);
            return new SongDTO(song, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<SocketIOClient> getClients(long userId, ConcurrentHashMap<SocketIOClient, Long> webSocketMap) {
        List<SocketIOClient> res = new ArrayList<>();
        for (SocketIOClient socketIOClient : webSocketMap.keySet()) {
            if (webSocketMap.get(socketIOClient) == userId) {
                res.add(socketIOClient);
            }
        }
        return res;
    }

    @Override
    public List<SongDTO> getSongList(long userId, String token) {
        List<SongDTO> res = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("list_userId").is(userId));
        List<Song> songs = mongoTemplate.find(query, Song.class, MusicServiceImpl.MUSIC_COLLECTION);
        for (Song s : songs) {
            SongDTO songDTO = new SongDTO(s, token);
            res.add(songDTO);
        }
        return res;
    }


}
