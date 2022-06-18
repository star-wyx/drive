package com.netdisk.module.DTO;

import com.netdisk.module.Song;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
public class SongDTO {

    private String source;

    private String name;

    private boolean shared;

    private boolean favorited;

    private long node_id;

    private long user_id;

    private String file_name;

    private String artist;

    private String cover;

    public SongDTO(Song song, String token) {
        ////todo change this
        this.source = "http://192.168.1.143:9090/vopen/" + song.getMd5() + "?token=" + token + "&fullfilename=" + song.getFileName();
        this.name = song.getName();
        this.shared = song.isShared();
        this.favorited = song.isFavorites();
        this.node_id = song.getNodeId();
        this.user_id = song.getUserId();
        this.file_name = song.getFileName();
        this.artist = song.getSinger();
        this.cover = song.getBase64();
    }
}
