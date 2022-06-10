package com.netdisk.module;

import com.netdisk.service.impl.MusicServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(MusicServiceImpl.MUSIC_COLLECTION)
public class Song {
    @Id
    private String id;

    private String fileName;

    private String md5;

    private String name;

    private String singer;

    private String base64;

    private Long userId;

    private Long nodeId;

    private Long list_userId;

    private boolean isShared;

    private boolean isFavorites;

}
